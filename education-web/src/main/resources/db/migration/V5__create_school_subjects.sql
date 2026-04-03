-- Предмети школи (створює адмін школи); використовуються у групах / курсах.

CREATE TABLE IF NOT EXISTS school_subjects (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  organization_id VARCHAR(36) NOT NULL,
  title VARCHAR(255) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_school_subject_org
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
  UNIQUE KEY ux_school_subject_org_title (organization_id, title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_school_subjects_org ON school_subjects(organization_id);

ALTER TABLE school_groups
  ADD COLUMN subject_id VARCHAR(36) NULL
  AFTER topics_label;

ALTER TABLE school_groups
  ADD CONSTRAINT fk_school_group_subject
    FOREIGN KEY (subject_id) REFERENCES school_subjects(id) ON DELETE SET NULL;

CREATE INDEX ix_school_groups_subject ON school_groups(subject_id);
