-- Викладач групи (опційно), посилання на `teachers`.

ALTER TABLE school_groups
  ADD COLUMN teacher_id VARCHAR(36) NULL
  AFTER subject_id;

ALTER TABLE school_groups
  ADD CONSTRAINT fk_school_group_teacher
  FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE SET NULL;

CREATE INDEX ix_school_groups_teacher ON school_groups(teacher_id);
