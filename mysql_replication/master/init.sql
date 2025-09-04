CREATE DATABASE IF NOT EXISTS bootdb;

-- Пользователь для репликации
CREATE USER IF NOT EXISTS 'repl'@'%' IDENTIFIED WITH mysql_native_password BY 'repl_pass';
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'repl'@'%';

-- Пользователь для мониторинга
CREATE USER IF NOT EXISTS 'monitor'@'%' IDENTIFIED BY 'monitor_pass';
GRANT PROCESS, REPLICATION SLAVE, REPLICATION CLIENT, RELOAD, SELECT ON *.* TO 'monitor'@'%';

-- Пользователь Orchestrator
CREATE USER IF NOT EXISTS 'orchestrator'@'%' IDENTIFIED BY 'orchestrator_pass';
GRANT PROCESS, REPLICATION CLIENT, RELOAD ON *.* TO 'orchestrator'@'%';

-- Пользователь приложения для ProxySQL
CREATE USER IF NOT EXISTS 'app'@'%' IDENTIFIED BY '14789';
GRANT ALL PRIVILEGES ON bootdb.* TO 'app'@'%';

-- Включаем плагин клонирования
INSTALL PLUGIN clone SONAME 'mysql_clone.so';

-- Пользователь для клонирования
CREATE USER IF NOT EXISTS 'clone_user'@'%' IDENTIFIED BY 'clone_pass';
GRANT BACKUP_ADMIN ON *.* TO 'clone_user'@'%';

FLUSH PRIVILEGES;

