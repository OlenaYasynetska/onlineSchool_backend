-- Здача ДЗ учнем (файл + предмет + текст); перевірка викладачем, зірки 1–3 на групу.
CREATE TABLE IF NOT EXISTS homework_portal_submissions (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  student_id VARCHAR(36) NOT NULL,
  teacher_id VARCHAR(36) NOT NULL COMMENT 'кому надіслано на перевірку',
  group_id VARCHAR(36) NULL COMMENT 'опційно — для нарахування зірок на картку групи',
  subject_title VARCHAR(255) NOT NULL,
  message_text TEXT NULL,
  file_name VARCHAR(255) NOT NULL,
  storage_path VARCHAR(1024) NOT NULL,
  content_type VARCHAR(128) NULL,
  file_size_bytes BIGINT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'submitted' COMMENT 'submitted|graded',
  stars TINYINT NULL COMMENT '1-3 після перевірки',
  teacher_feedback TEXT NULL,
  submitted_at TIMESTAMP(6) NOT NULL,
  graded_at TIMESTAMP(6) NULL,
  graded_by_teacher_id VARCHAR(36) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_hw_portal_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
  CONSTRAINT fk_hw_portal_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE RESTRICT,
  CONSTRAINT fk_hw_portal_group FOREIGN KEY (group_id) REFERENCES school_groups(id) ON DELETE SET NULL,
  CONSTRAINT fk_hw_portal_grader FOREIGN KEY (graded_by_teacher_id) REFERENCES teachers(id) ON DELETE SET NULL,
  CONSTRAINT chk_hw_portal_stars CHECK (stars IS NULL OR stars BETWEEN 1 AND 3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_hw_portal_teacher_status ON homework_portal_submissions (
  teacher_id,
  status,
  submitted_at DESC
);
CREATE INDEX ix_hw_portal_student ON homework_portal_submissions (student_id);
