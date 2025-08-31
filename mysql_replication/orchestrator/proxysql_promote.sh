#!/bin/sh

NEW_MASTER_HOST=$1
NEW_MASTER_PORT=$2

# Снять read_only на новом мастере
mysql -h "$NEW_MASTER_HOST" -P "$NEW_MASTER_PORT" -u monitor -pmonitor_pass -e "SET GLOBAL read_only=OFF;"

# Обновляем hostgroup в ProxySQL через SQL-интерфейс
mysql -uadmin -padmin -h proxysql -P6032 <<EOF
-- Новый мастер в writer hostgroup
UPDATE mysql_servers SET hostgroup_id=10 WHERE hostname='$NEW_MASTER_HOST' AND port=$NEW_MASTER_PORT;

-- Все остальные переводим в reader hostgroup
UPDATE mysql_servers SET hostgroup_id=20 WHERE NOT (hostname='$NEW_MASTER_HOST' AND port=$NEW_MASTER_PORT);

LOAD MYSQL SERVERS TO RUNTIME;
SAVE MYSQL SERVERS TO DISK;
EOF

echo "ProxySQL updated: $NEW_MASTER_HOST:$NEW_MASTER_PORT → writer (HG=10)"
