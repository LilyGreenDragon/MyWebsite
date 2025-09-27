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
} >> $LOG_FILE 2>&1


# --- Новый мастер ---
mysql -h proxysql -P 6032 -u remote_admin -premote_pass -e "
UPDATE mysql_servers SET hostgroup_id=10, status='ONLINE'
WHERE hostname='${ORC_SUCCESSOR_HOST}';
" >> $LOG_FILE 2>&1

# --- Все остальные слейвы → reader ---
mysql -h proxysql -P 6032 -u remote_admin -premote_pass -e "
UPDATE mysql_servers SET hostgroup_id=20, status='ONLINE'
WHERE hostname!='${ORC_SUCCESSOR_HOST}';
" >> $LOG_FILE 2>&1

mysql -h proxysql -P 6032 -u remote_admin -premote_pass -e "
    UPDATE mysql_servers SET status='OFFLINE_HARD'
    WHERE hostname='${ORC_FAILED_HOST}';
    " >> $LOG_FILE 2>&1

mysql -h proxysql -P 6032 -u remote_admin -premote_pass -e "
LOAD MYSQL SERVERS TO RUNTIME;
SAVE MYSQL SERVERS TO DISK;

SELECT 'ProxySQL configuration updated' as result;
SELECT hostgroup_id, hostname, port, status FROM mysql_servers ORDER BY hostgroup_id, hostname;
SELECT hostgroup_id, hostname, port, status FROM runtime_mysql_servers ORDER BY hostgroup_id, hostname;
" >> $LOG_FILE 2>&1

# Проверка кода возврата mysql
if [ $? -ne 0 ]; then
  echo "ProxySQL update failed!" >> $LOG_FILE
  exit 1
fi

echo "Script completed at $(date)" >> $LOG_FILE

