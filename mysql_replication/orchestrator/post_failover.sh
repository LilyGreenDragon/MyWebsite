#!/bin/bash

LOG_FILE="/tmp/orchestrator-post-failover.log"
# Очистка файла лога перед каждым запуском
truncate -s 0 $LOG_FILE

{
echo "=== PROXYSQL PROMOTE SCRIPT STARTED ==="
echo "Time: $(date)"
# Ждём, пока Orchestrator зафиксирует успешный failover
MAX_WAIT=60        # максимальное время ожидания (в секундах)
SLEEP_INTERVAL=2   # сколько секунд спать между проверками
WAITED=0           # сколько всего уже ждали

while true; do
    if [ "$ORC_IS_SUCCESSFUL" == "true" ] && [ -n "$ORC_SUCCESSOR_HOST" ]; then
        echo "Failover завершён. Новый мастер: $ORC_SUCCESSOR_HOST:$ORC_SUCCESSOR_PORT" >> $LOG_FILE
        break
    fi

    if [ "$WAITED" -ge "$MAX_WAIT" ]; then
        echo "Timeout ожидания failover!" >> $LOG_FILE
        exit 1
    fi

    sleep $SLEEP_INTERVAL
    WAITED=$((WAITED+SLEEP_INTERVAL))
done

echo "--- Orchestrator environment variables ---"
env | grep ORC_ | sort
echo "-----------------------------------------"
echo "New Master: ${ORC_SUCCESSOR_HOST}:${ORC_SUCCESSOR_PORT}"
echo "Old Master: ${ORC_FAILED_HOST}:${ORC_FAILED_PORT}"

# --- Все серверы переводим в hostgroup 20 ---
mysql -h proxysql -P 6032 -u remote_admin -premote_pass -e "
UPDATE mysql_servers
SET hostgroup_id = 20,
    status = CASE
        WHEN hostname = '${ORC_FAILED_HOST}' AND port='${ORC_FAILED_PORT}' THEN 'OFFLINE_HARD'
        ELSE 'ONLINE'
    END;"

# Проверка кода возврата mysql
if [ $? -ne 0 ]; then
  echo "ProxySQL update(all hostname in hostgroup 20,status=ONLINE, former MASTER status=OFFLINE_HARD) failed!"
  exit 1
else
  echo "All servers moved to read-only mode(all hostname in hostgroup 20,status=ONLINE, former MASTER status=OFFLINE_HARD)"
fi

# --- Применяем изменения ---
mysql -h proxysql -P 6032 -u remote_admin -premote_pass -e "
LOAD MYSQL SERVERS TO RUNTIME;
SAVE MYSQL SERVERS TO DISK;"
sleep 10

# =================================================================
# ОСТАНОВКА РЕПЛИКАЦИИ НА НОВОМ МАСТЕРЕ
# =================================================================

echo "=== Stopping replication on new master ==="

# Останавливаем репликацию на новом мастере через прямое подключение
mysql -h $ORC_SUCCESSOR_HOST -u repl -prepl_pass -e "
STOP REPLICA;"


if [ $? -eq 0 ]; then
    echo "✅ Replication stopped on new master: $ORC_SUCCESSOR_HOST"
else
    echo "⚠️  Failed to stop replication on new master"
fi
sleep 10

# Проверяем что репликация остановлена
REPLICA_STATUS=$(mysql -h $ORC_SUCCESSOR_HOST -u repl -prepl_pass -sN -e "SHOW REPLICA STATUS\G" 2>/dev/null | wc -l)

if [ "$REPLICA_STATUS" -eq 0 ]; then
    echo "✅ Confirmed: No replication running on new master"
else
    echo "❌ Replication still running on new master"
fi

sleep 10
mysql -h $ORC_SUCCESSOR_HOST -u repl -prepl_pass -e "
    SET GLOBAL super_read_only = OFF;
    SET GLOBAL read_only = OFF;"

sleep 10
# --- Новый мастер ---
mysql -h proxysql -P 6032 -u remote_admin -premote_pass -e "
UPDATE mysql_servers SET hostgroup_id=10, status='ONLINE'
WHERE hostname='${ORC_SUCCESSOR_HOST}' AND port='${ORC_SUCCESSOR_PORT}';"

# Проверка кода возврата mysql
if [ $? -ne 0 ]; then
  echo "ProxySQL update(new master in hostgroup_id=10) failed!"
  exit 1
else
  echo "New master in hostgroup_id=10"
fi

# --- Применяем изменения ---
mysql -h proxysql -P 6032 -u remote_admin -premote_pass -e "
LOAD MYSQL SERVERS TO RUNTIME;
SAVE MYSQL SERVERS TO DISK;"
sleep 10

mysql -h proxysql -P 6032 -u remote_admin -premote_pass -e "
SELECT 'ProxySQL configuration updated' as result;
SELECT hostgroup_id, hostname, port, status FROM mysql_servers ORDER BY hostgroup_id, hostname;
SELECT hostgroup_id, hostname, port, status FROM runtime_mysql_servers ORDER BY hostgroup_id, hostname;"

echo "Script completed at $(date)"

} >> $LOG_FILE 2>&1
