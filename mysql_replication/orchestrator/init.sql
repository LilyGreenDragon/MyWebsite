-- создаём базу для Orchestrator
CREATE DATABASE IF NOT EXISTS orchestrator CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- создаём пользователя orchestrator
CREATE USER IF NOT EXISTS 'orchestrator'@'%' IDENTIFIED BY 'orchestrator_pass';
GRANT ALL PRIVILEGES ON orchestrator.* TO 'orchestrator'@'%';

FLUSH PRIVILEGES;