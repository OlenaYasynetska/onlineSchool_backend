CREATE TABLE study_material_sets (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    school_id VARCHAR(36) NOT NULL,
    teacher_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP(3) NOT NULL,
    updated_at TIMESTAMP(3) NOT NULL,
    KEY idx_study_material_sets_school (school_id),
    KEY idx_study_material_sets_teacher (teacher_id),
    CONSTRAINT fk_study_material_sets_school FOREIGN KEY (school_id) REFERENCES organizations (id),
    CONSTRAINT fk_study_material_sets_teacher FOREIGN KEY (teacher_id) REFERENCES teachers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE study_material_lessons (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    material_set_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    pdf_data LONGBLOB NOT NULL,
    file_name VARCHAR(512) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL,
    updated_at TIMESTAMP(3) NOT NULL,
    KEY idx_study_material_lessons_set (material_set_id),
    CONSTRAINT fk_study_material_lessons_set FOREIGN KEY (material_set_id) REFERENCES study_material_sets (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
