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

# =================================================================
# ФИКСАЦИЯ ДВОЙНОГО СЛЕША В РЕПЛИКАЦИИ
# =================================================================

echo "=== Fixing double slash in replication ==="
echo "Time: $(date)"

# Автоматически определяем все слейвы из ProxySQL
echo "Detecting slaves from ProxySQL..."
SLAVES=$(mysql -h proxysql -P 6032 -u remote_admin -premote_pass -sN -e "
SELECT hostname
FROM runtime_mysql_servers
WHERE hostgroup_id = 20
AND status = 'ONLINE'
AND hostname != '${ORC_FAILED_HOST}';")

if [ -z "$SLAVES" ]; then
    echo "No slaves found in ProxySQL configuration"
    exit 0
fi

echo "Found slaves: $SLAVES"

# Исправляем на всех обнаруженных слейвах
for SLAVE in $SLAVES; do
    echo "Checking $SLAVE..."

    # Получаем Master_Host через прямое подключение к MySQL
    MASTER_HOST=$(mysql -h $SLAVE -u root -p147891 -e "SHOW REPLICA STATUS\G" 2>/dev/null | grep -i "Master_Host" | awk '{print $2}')

    if [ -z "$MASTER_HOST" ]; then
        echo "❌ $SLAVE: No replication configured or cannot connect"
        continue
    fi

    echo "$SLAVE: Master_Host = '$MASTER_HOST'"

    if [[ "$MASTER_HOST" == //* ]]; then
        echo "❌ Found double slash on $SLAVE: $MASTER_HOST"
        echo "Fixing: $MASTER_HOST -> ${MASTER_HOST#//}"

        # Исправляем убирая // через прямое подключение
        mysql -h $SLAVE -u root -p147891 -e "
        STOP REPLICA;
        CHANGE REPLICATION SOURCE TO
          SOURCE_HOST='${MASTER_HOST#//}',
          SOURCE_USER='repl',
          SOURCE_PASSWORD='repl_pass',
          SOURCE_AUTO_POSITION=1;
        START REPLICA;
        " 2>/dev/null

        if [ $? -eq 0 ]; then
            echo "✅ Fixed replication on $SLAVE"

            # Проверяем что исправление применилось
            sleep 3
            NEW_MASTER_HOST=$(mysql -h $SLAVE -u root -p147891 -e "SHOW REPLICA STATUS\G" 2>/dev/null | grep -i "Master_Host" | awk '{print $2}')
            REPLICA_IO=$(mysql -h $SLAVE -u root -p147891 -sN -e "SHOW REPLICA STATUS\G" 2>/dev/null | grep -i "Slave_IO_Running" | awk '{print $2}')
            REPLICA_SQL=$(mysql -h $SLAVE -u root -p147891 -sN -e "SHOW REPLICA STATUS\G" 2>/dev/null | grep -i "Slave_SQL_Running" | awk '{print $2}')

            echo "✅ After fix - Source_Host: $NEW_MASTER_HOST, IO: $REPLICA_IO, SQL: $REPLICA_SQL"
        else
            echo "❌ Failed to fix replication on $SLAVE"
        fi
    else
        echo "✅ $SLAVE has correct Source_Host: $MASTER_HOST"
    fi
done

echo "Double slash fix completed at $(date)"

# =================================================================
# ОСТАНОВКА РЕПЛИКАЦИИ НА НОВОМ МАСТЕРЕ
# =================================================================

echo "=== Stopping replication on new master ==="
echo "Time: $(date)"

# Останавливаем репликацию на новом мастере через прямое подключение
mysql -h $ORC_SUCCESSOR_HOST -u root -p147891 -e "
STOP REPLICA;
RESET REPLICA ALL;
" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ Replication stopped on new master: $ORC_SUCCESSOR_HOST"
else
    echo "⚠️  Failed to stop replication on new master"
fi

# Проверяем что репликация остановлена
REPLICA_STATUS=$(mysql -h $ORC_SUCCESSOR_HOST -u root -p147891 -sN -e "SHOW REPLICA STATUS\G" 2>/dev/null | wc -l)

if [ "$REPLICA_STATUS" -eq 0 ]; then
    echo "✅ Confirmed: No replication running on new master"
else
    echo "❌ Replication still running on new master"
fi

} >> $LOG_FILE 2>&1