-- Окремий рядок на кожен предмет викладача (замість одного поля teachers.subject).

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

-- Перенесення з teachers.subject лише якщо колонка ще є (унікальний запуск Flyway).
SET @has_subject_col := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'teachers'
    AND COLUMN_NAME = 'subject'
);

SET @sql_migrate := IF(
  @has_subject_col > 0,
  'INSERT INTO teacher_subjects (id, teacher_id, title, sort_order, created_at) '
    'SELECT UUID(), t.id, TRIM(t.subject), 0, t.created_at FROM teachers t '
    'WHERE t.subject IS NOT NULL AND LENGTH(TRIM(t.subject)) > 0',
  'SELECT 1'
);
PREPARE stmt_migrate FROM @sql_migrate;
EXECUTE stmt_migrate;
DEALLOCATE PREPARE stmt_migrate;

SET @sql_drop := IF(
  @has_subject_col > 0,
  'ALTER TABLE teachers DROP COLUMN subject',
  'SELECT 1'
);
PREPARE stmt_drop FROM @sql_drop;
EXECUTE stmt_drop;
DEALLOCATE PREPARE stmt_drop;
