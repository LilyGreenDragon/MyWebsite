CREATE TABLE IF NOT EXISTS person_lessons (
person_id INT NOT NULL,
lesson_id BIGINT NOT NULL,
FOREIGN KEY (person_id) REFERENCES person (id) ON DELETE CASCADE,
FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE CASCADE,
PRIMARY KEY (person_id, lesson_id)
);