-- Якщо таблиці teacher_subjects немає (Flyway V7 ще не виконувався), виконайте цей скрипт
-- у схемі `schools` вручну в MySQL Workbench, потім перезапустіть бекенд — Flyway може
-- позначити V7 як виконану або ви вирівняєте flyway_schema_history вручну.

CREATE TABLE IF NOT EXISTS teacher_subjects (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  teacher_id VARCHAR(36) NOT NULL,
  title VARCHAR(255) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_teacher_subject_teacher
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE,
  INDEX ix_teacher_subjects_teacher (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
