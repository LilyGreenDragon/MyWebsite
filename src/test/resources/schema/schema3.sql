CREATE TABLE IF NOT EXISTS person_roles(
person_id INT not null,
role_id INT not null,
FOREIGN KEY (person_id) REFERENCES person (id),
FOREIGN KEY (role_id) REFERENCES roles (id),
PRIMARY KEY (person_id, role_id)
);
