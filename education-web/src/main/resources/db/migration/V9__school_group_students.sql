-- Зв'язок студент ↔ група (після створення студента його додають до існуючої групи).

CREATE TABLE IF NOT EXISTS school_group_students (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  student_id VARCHAR(36) NOT NULL,
  group_id VARCHAR(36) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  UNIQUE KEY ux_school_group_student (student_id, group_id),
  CONSTRAINT fk_sgs_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
  CONSTRAINT fk_sgs_group FOREIGN KEY (group_id) REFERENCES school_groups(id) ON DELETE CASCADE,
  INDEX ix_sgs_group (group_id),
  INDEX ix_sgs_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
