CREATE DATABASE IF NOT EXISTS bootdb;

-- Пользователь для репликации
CREATE USER IF NOT EXISTS 'repl'@'%' IDENTIFIED WITH mysql_native_password BY 'repl_pass';
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'repl'@'%';

-- Пользователь для мониторинга
CREATE USER IF NOT EXISTS 'monitor'@'%' IDENTIFIED BY 'monitor_pass';
GRANT PROCESS, REPLICATION SLAVE, REPLICATION CLIENT, RELOAD, SELECT ON *.* TO 'monitor'@'%';

-- Пользователь Orchestrator
CREATE USER IF NOT EXISTS 'orchestrator'@'%' IDENTIFIED BY 'orchestrator_pass';
GRANT PROCESS, RELOAD, REPLICATION SLAVE, REPLICATION CLIENT,
      REPLICATION_SLAVE_ADMIN,  -- для CHANGE/RESET REPLICATION
      CONNECTION_ADMIN,         -- чтобы рвать коннекты при свитче
      SYSTEM_VARIABLES_ADMIN,   -- чтобы менять read_only/super_read_only
      SESSION_VARIABLES_ADMIN,
      BINLOG_ADMIN              -- на всякий случай для бинлог-операций
ON *.* TO 'orchestrator'@'%';

-- Пользователь приложения для ProxySQL
CREATE USER IF NOT EXISTS 'app'@'%' IDENTIFIED BY '14789';
GRANT ALL PRIVILEGES ON bootdb.* TO 'app'@'%';

-- Включаем плагин клонирования
INSTALL PLUGIN clone SONAME 'mysql_clone.so';

-- Пользователь для клонирования
CREATE USER IF NOT EXISTS 'clone_user'@'%' IDENTIFIED BY 'clone_pass';
GRANT BACKUP_ADMIN ON *.* TO 'clone_user'@'%';

FLUSH PRIVILEGES;

