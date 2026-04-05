-- Розклад занять по групах (курсах) + домашні завдання, здачі та оцінки 1–3 (зірки в UI).

-- ---------------------------------------------------------------------------
-- 1) Розклад: повторювані слоти (день тижня + час). Адмін школи / учитель.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS school_group_schedule (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  group_id VARCHAR(36) NOT NULL,
  day_of_week TINYINT NOT NULL COMMENT '1=Пн … 7=Нд (ISO)',
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  valid_from DATE NULL COMMENT 'NULL = без обмеження з початку',
  valid_until DATE NULL COMMENT 'NULL = без кінцевої дати',
  notes VARCHAR(500) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_sgsch_group
    FOREIGN KEY (group_id) REFERENCES school_groups(id) ON DELETE CASCADE,
  CONSTRAINT chk_sgsch_dow CHECK (day_of_week BETWEEN 1 AND 7),
  CONSTRAINT chk_sgsch_time CHECK (start_time < end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Ідемпотентно (MySQL 5.7: немає DROP INDEX IF EXISTS — створюємо індекс лише якщо його ще немає).
SET @db = DATABASE();
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = @db AND table_name = 'school_group_schedule' AND index_name = 'ix_school_group_schedule_group') = 0,
  'CREATE INDEX ix_school_group_schedule_group ON school_group_schedule(group_id)',
  'SELECT 1'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- 2) Домашнє завдання по групі (видає учитель; видимість — учасникам групи).
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS homework_assignments (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  group_id VARCHAR(36) NOT NULL,
  teacher_id VARCHAR(36) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT NULL,
  due_at TIMESTAMP(6) NULL COMMENT 'дедлайн здачі',
  max_points TINYINT NOT NULL DEFAULT 3 COMMENT 'макс. зірок у UI (1–3)',
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_hw_assign_group
    FOREIGN KEY (group_id) REFERENCES school_groups(id) ON DELETE CASCADE,
  CONSTRAINT fk_hw_assign_teacher
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE RESTRICT,
  CONSTRAINT chk_hw_max_points CHECK (max_points BETWEEN 1 AND 3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @db = DATABASE();
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = @db AND table_name = 'homework_assignments' AND index_name = 'ix_homework_assignments_group') = 0,
  'CREATE INDEX ix_homework_assignments_group ON homework_assignments(group_id)',
  'SELECT 1'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = @db AND table_name = 'homework_assignments' AND index_name = 'ix_homework_assignments_teacher') = 0,
  'CREATE INDEX ix_homework_assignments_teacher ON homework_assignments(teacher_id)',
  'SELECT 1'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = @db AND table_name = 'homework_assignments' AND index_name = 'ix_homework_assignments_due') = 0,
  'CREATE INDEX ix_homework_assignments_due ON homework_assignments(due_at)',
  'SELECT 1'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- 3) Здача студентом + оцінка / відгук учителя (один рядок на пару завдання+студент).
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS homework_submissions (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  assignment_id VARCHAR(36) NOT NULL,
  student_id VARCHAR(36) NOT NULL,
  answer_text TEXT NULL,
  attachment_url VARCHAR(1024) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT 'draft|submitted|graded',
  submitted_at TIMESTAMP(6) NULL,
  score TINYINT NULL COMMENT '1–3 зірки після перевірки',
  feedback TEXT NULL COMMENT 'відгук учителя',
  graded_at TIMESTAMP(6) NULL,
  graded_by_teacher_id VARCHAR(36) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_hw_sub_assign
    FOREIGN KEY (assignment_id) REFERENCES homework_assignments(id) ON DELETE CASCADE,
  CONSTRAINT fk_hw_sub_student
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
  CONSTRAINT fk_hw_sub_grader
    FOREIGN KEY (graded_by_teacher_id) REFERENCES teachers(id) ON DELETE SET NULL,
  UNIQUE KEY ux_hw_submission_assign_student (assignment_id, student_id),
  CONSTRAINT chk_hw_score CHECK (score IS NULL OR score BETWEEN 1 AND 3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @db = DATABASE();
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = @db AND table_name = 'homework_submissions' AND index_name = 'ix_homework_submissions_student') = 0,
  'CREATE INDEX ix_homework_submissions_student ON homework_submissions(student_id)',
  'SELECT 1'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = @db AND table_name = 'homework_submissions' AND index_name = 'ix_homework_submissions_assign') = 0,
  'CREATE INDEX ix_homework_submissions_assign ON homework_submissions(assignment_id)',
  'SELECT 1'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
