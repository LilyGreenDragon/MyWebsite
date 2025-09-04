#!/bin/bash
#
# proxysql_promote.sh <new_master_host> <new_master_port>
#
# Этот скрипт вызывается Orchestrator после failover.
# Он обновляет ProxySQL: новый мастер попадает в writer HG (10), остальные - в reader HG (20).
#

NEW_MASTER="$1"
NEW_MASTER_PORT="$2"

# ProxySQL доступ
PROXYSQL_HOST="proxysql"
PROXYSQL_PORT="6032"
PROXYSQL_USER="admin"
PROXYSQL_PASS="admin"

# Orchestrator API
ORC_API="http://orchestrator:3000/api"

# Хостгруппы
WRITER_HG=10
READER_HG=20

echo "[INFO] Новый мастер: $NEW_MASTER:$NEW_MASTER_PORT"

# 1. Получаем список всех живых инстансов кластера из Orchestrator
CLUSTER=$(curl -s "$ORC_API/cluster/$NEW_MASTER/$NEW_MASTER_PORT" | jq -r '.[].Key.Hostname + ":" + (.[].Key.Port|tostring)')

if [ -z "$CLUSTER" ]; then
    echo "[ERROR] Не удалось получить список нод из Orchestrator"
    exit 1
fi

# 2. Чистим все старые записи в ProxySQL
mysql -h $PROXYSQL_HOST -P $PROXYSQL_PORT -u $PROXYSQL_USER -p$PROXYSQL_PASS -e "DELETE FROM mysql_servers;"

# 3. Добавляем новый мастер
mysql -h $PROXYSQL_HOST -P $PROXYSQL_PORT -u $PROXYSQL_USER -p$PROXYSQL_PASS -e "
    INSERT INTO mysql_servers (hostgroup_id, hostname, port, status)
    VALUES ($WRITER_HG, '$NEW_MASTER', $NEW_MASTER_PORT, 'ONLINE');"

# 4. Добавляем остальных как реплики
for NODE in $CLUSTER; do
    HOST=$(echo $NODE | cut -d: -f1)
    PORT=$(echo $NODE | cut -d: -f2)

    if [[ "$HOST" != "$NEW_MASTER" || "$PORT" != "$NEW_MASTER_PORT" ]]; then
        mysql -h $PROXYSQL_HOST -P $PROXYSQL_PORT -u $PROXYSQL_USER -p$PROXYSQL_PASS -e "
            INSERT INTO mysql_servers (hostgroup_id, hostname, port, status)
            VALUES ($READER_HG, '$HOST', $PORT, 'ONLINE');"
    fi
done

# 5. Сохраняем изменения
mysql -h $PROXYSQL_HOST -P $PROXYSQL_PORT -u $PROXYSQL_USER -p$PROXYSQL_PASS -e "LOAD MYSQL SERVERS TO RUNTIME; SAVE MYSQL SERVERS TO DISK;"

echo "[INFO] ProxySQL обновлен: мастер → $NEW_MASTER:$NEW_MASTER_PORT"
