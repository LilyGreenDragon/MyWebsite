-- Пользователь для репликации
CREATE USER IF NOT EXISTS 'repl'@'%' IDENTIFIED WITH mysql_native_password BY 'repl_pass';
GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%';

-- Пользователь для мониторинга
CREATE USER IF NOT EXISTS 'monitor'@'%' IDENTIFIED BY 'monitor_pass';
GRANT SUPER, PROCESS, REPLICATION SLAVE, REPLICATION CLIENT, RELOAD, SELECT ON *.* TO 'monitor'@'%';

-- Пользователь Orchestrator
CREATE USER IF NOT EXISTS 'orchestrator'@'%' IDENTIFIED BY 'orchestrator_pass';
GRANT SUPER, PROCESS, REPLICATION CLIENT, RELOAD ON *.* TO 'orchestrator'@'%';

-- Пользователь приложения для ProxySQL
CREATE USER IF NOT EXISTS 'app'@'%' IDENTIFIED BY '14789';
GRANT ALL PRIVILEGES ON bootdb.* TO 'app'@'%';

FLUSH PRIVILEGES;