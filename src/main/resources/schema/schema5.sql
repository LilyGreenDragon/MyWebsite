CREATE TABLE IF NOT EXISTS dictionary (
    name VARCHAR(50) PRIMARY KEY NOT NULL,
    meaning VARCHAR(250) NOT NULL
);

-- вставка строки, если таблица пуста
INSERT INTO dictionary (name, meaning)
SELECT 'password', '3011' WHERE NOT EXISTS (SELECT 1 FROM dictionary WHERE name = 'password');