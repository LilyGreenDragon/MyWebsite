CREATE TABLE IF NOT EXISTS person(
id INT PRIMARY KEY auto_increment,
username VARCHAR(30) not null unique,
password VARCHAR(250) not null,
email VARCHAR(50),
name VARCHAR(50),
surname VARCHAR(50),
birthdate DATE,
photo VARCHAR(250),
image_theme VARCHAR(250)
);
