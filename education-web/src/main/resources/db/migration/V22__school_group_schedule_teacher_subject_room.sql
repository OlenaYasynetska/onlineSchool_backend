-- Додаткові поля для слотів розкладу: викладач предмету, предмет, кабінет.

ALTER TABLE school_group_schedule
  ADD COLUMN teacher_id VARCHAR(36) NULL COMMENT 'FK teachers' AFTER group_id,
  ADD COLUMN subject_id VARCHAR(36) NULL COMMENT 'FK school_subjects' AFTER teacher_id;

ALTER TABLE school_group_schedule
  ADD CONSTRAINT fk_sgsch_teacher
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE SET NULL,
  ADD CONSTRAINT fk_sgsch_subject
    FOREIGN KEY (subject_id) REFERENCES school_subjects(id) ON DELETE SET NULL;

ALTER TABLE school_group_schedule
  ADD COLUMN room VARCHAR(255) NULL AFTER notes;

SET @db = DATABASE();
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = @db AND table_name = 'school_group_schedule' AND index_name = 'ix_school_group_schedule_teacher') = 0,
  'CREATE INDEX ix_school_group_schedule_teacher ON school_group_schedule(teacher_id)',
  'SELECT 1'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = @db AND table_name = 'school_group_schedule' AND index_name = 'ix_school_group_schedule_subject') = 0,
  'CREATE INDEX ix_school_group_schedule_subject ON school_group_schedule(subject_id)',
  'SELECT 1'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
