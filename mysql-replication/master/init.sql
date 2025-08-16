-- создаём тестовую БД
CREATE DATABASE IF NOT EXISTS bootdb;

-- пользователь для репликации (только с SSL)
CREATE USER IF NOT EXISTS 'replica'@'%'
  IDENTIFIED WITH caching_sha2_password BY 'replica_pass' REQUIRE SSL;
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'replica'@'%';

-- пользователь для ProxySQL мониторинга (только с SSL)
CREATE USER IF NOT EXISTS 'monitor'@'%'
  IDENTIFIED WITH caching_sha2_password BY 'monitor_pass' REQUIRE SSL;
GRANT USAGE, REPLICATION CLIENT ON *.* TO 'monitor'@'%';

-- применяем изменения
FLUSH PRIVILEGES;
