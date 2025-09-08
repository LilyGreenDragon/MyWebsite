#!/bin/bash

NEW_MASTER_HOST="${ORC_FAILOVER_MASTER_HOST}"
NEW_MASTER_PORT="${ORC_FAILOVER_MASTER_PORT}"
OLD_MASTER_HOST="${ORC_FAILOVER_OLD_MASTER_HOST}"
OLD_MASTER_PORT="${ORC_FAILOVER_OLD_MASTER_PORT}"

mysql -h proxysql -P 6032 -u remote_admin -premote_pass -e "
-- Новый мастер становится писателем (hostgroup 10)
UPDATE mysql_servers
SET hostgroup_id = 10, status = 'ONLINE'
WHERE hostname = '${NEW_MASTER_HOST}' AND port = ${NEW_MASTER_PORT};

-- Старый мастер становится читателем или отключается
UPDATE mysql_servers
SET hostgroup_id = 20, status = 'OFFLINE_SOFT'
WHERE hostname = '${OLD_MASTER_HOST}' AND port = ${OLD_MASTER_PORT};

-- Применяем изменения
LOAD MYSQL SERVERS TO RUNTIME;
SAVE MYSQL SERVERS TO DISK;
"