#!/bin/bash

LOG_FILE="/tmp/orchestrator-pre-failover.log"
# Очистка файла лога перед каждым запуском
truncate -s 0 $LOG_FILE

echo "=== BLOCKING WRITES BEFORE FAILOVER ===" >> $LOG_FILE
echo "Time: $(date)" >> $LOG_FILE
echo "Failed master: $ORC_FAILED_HOST:$ORC_FAILED_PORT" >> $LOG_FILE
echo "Cluster: $ORC_CLUSTER_ALIAS" >> $LOG_FILE

#  Переводим упавший мастер в hostgroup для чтения (20)
echo "Moving failed master to read-only hostgroup..." >> $LOG_FILE
mysql -h proxysql -P 6032 -u remote_admin -premote_pass -e "
UPDATE mysql_servers SET hostgroup_id = 20, status = 'ONLINE'
WHERE hostname = '$ORC_FAILED_HOST' AND port = $ORC_FAILED_PORT;
LOAD MYSQL SERVERS TO RUNTIME;
" >> $LOG_FILE 2>&1

exit 0