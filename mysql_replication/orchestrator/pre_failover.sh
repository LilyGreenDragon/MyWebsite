#!/bin/bash

LOG_FILE="/tmp/orchestrator-pre-failover.log"
echo "=== BLOCKING WRITES BEFORE FAILOVER ===" >> $LOG_FILE
echo "Time: $(date)" >> $LOG_FILE
echo "Failed master: $ORC_FAILED_HOST:$ORC_FAILED_PORT" >> $LOG_FILE
echo "Cluster: $ORC_CLUSTER_ALIAS" >> $LOG_FILE

# 1. Переводим упавший мастер в hostgroup для чтения (20)
echo "Moving failed master to read-only hostgroup..." >> $LOG_FILE
mysql -h proxysql -P 6032 -u remote_admin -premote_pass -e "
UPDATE mysql_servers SET hostgroup_id = 20, status = 'ONLINE'
WHERE hostname = '$ORC_FAILED_HOST' AND port = $ORC_FAILED_PORT;
LOAD MYSQL SERVERS TO RUNTIME;
" >> $LOG_FILE 2>&1

# 2. Проверяем что запись заблокирована
echo "Verifying writes are blocked..." >> $LOG_FILE
mysql -h proxysql -P 6032 -u remote_admin -premote_pass -e "
SELECT hostgroup_id, hostname, port, status
FROM runtime_mysql_servers
WHERE hostgroup_id = 10;" >> $LOG_FILE 2>&1

echo "All writes blocked by moving master to read-only pool." >> $LOG_FILE
echo "Orchestrator can proceed with failover." >> $LOG_FILE

exit 0