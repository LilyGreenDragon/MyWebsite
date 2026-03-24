CREATE TABLE IF NOT EXISTS lessons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_name VARCHAR(100) NOT NULL,
    lesson_name VARCHAR(100) NOT NULL,
    room_name VARCHAR(50) NOT NULL,
    day_of_week TINYINT NOT NULL,  -- 1=пн, 2=вт, 3=ср, 4=чт, 5=пт, 6=сб, 7=вс
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
    );

INSERT INTO lessons (teacher_name, lesson_name, room_name, day_of_week, start_time, end_time) VALUES
    -- Понедельник (day_of_week = 1)
    ('Матвеева Н.С.', 'Йога', 'Малый зал', 1, '11:00', '12:00'),
    ('Ванова А.В.', 'Рисование для малышей', 'Кабинет 2', 1, '11:00', '11:40'),
    ('Осипов П.Д.', 'Футбол', 'Большой зал', 1, '13:00', '14:00'),

    -- Вторник (day_of_week = 2)
    ('Матвеева Н.С.', 'Йога(старшая группа)', 'Малый зал', 2, '10:00', '12:00'),
    ('Панова В.А.', 'Ритмика', 'Малый зал', 2, '15:00', '16:00');