-- Обліковий запис учня в `users` (логін); NULL для старих рядків без акаунта.
ALTER TABLE students
ADD COLUMN user_id VARCHAR(36) NULL AFTER school_id;

CREATE UNIQUE INDEX ux_students_user_id ON students (user_id);

ALTER TABLE students
ADD CONSTRAINT fk_students_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
