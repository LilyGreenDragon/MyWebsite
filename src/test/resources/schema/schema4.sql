INSERT INTO roles (name) VALUES ('ADMIN'), ('USER'), ('BLOCKED');
INSERT INTO person (id, username, password, email) VALUES (1, 'Test User', 'password', 'test@mail.ru');
INSERT INTO person_roles (person_id,role_id) VALUES (1, 2);
INSERT INTO person (id, username, password, email) VALUES (2, 'Test Admin', 'password', 'test@mail.ru');
INSERT INTO person_roles (person_id,role_id) VALUES (2, 1);