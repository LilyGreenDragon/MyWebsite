-- создаём тестовую БД, если нужно
CREATE DATABASE IF NOT EXISTS bootdb;

-- создаём пользователя репликации (для консистентности, даже если слейв сам никого не обслуживает)
CREATE USER IF NOT EXISTS 'replica'@'%'
  IDENTIFIED WITH caching_sha2_password BY 'replica_pass' REQUIRE SSL;
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'replica'@'%';

-- создаём пользователя мониторинга для ProxySQL
CREATE USER IF NOT EXISTS 'monitor'@'%'
  IDENTIFIED WITH caching_sha2_password BY 'monitor_pass' REQUIRE SSL;
GRANT USAGE, REPLICATION CLIENT ON *.* TO 'monitor'@'%';

-- применяем изменения
FLUSH PRIVILEGES;

-- настраиваем репликацию
STOP REPLICA;

CHANGE REPLICATION SOURCE TO
    SOURCE_HOST='master',
    SOURCE_USER='replica',
    SOURCE_PASSWORD='replica_pass',
    SOURCE_PORT=3306,
    SOURCE_AUTO_POSITION=1,
    SOURCE_SSL=1,
    SOURCE_SSL_CA='/etc/mysql/certs/ca.pem',
    SOURCE_SSL_CERT='/etc/mysql/certs/client-cert.pem',
    SOURCE_SSL_KEY='/etc/mysql/certs/client-key.pem';

START REPLICA;



