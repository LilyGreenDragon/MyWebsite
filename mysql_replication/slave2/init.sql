-- Настройка репликации slave → master
CHANGE REPLICATION SOURCE TO
  SOURCE_HOST='mysql-master',
  SOURCE_USER='repl',
  SOURCE_PASSWORD='repl_pass',
  SOURCE_AUTO_POSITION=1;

START REPLICA;
