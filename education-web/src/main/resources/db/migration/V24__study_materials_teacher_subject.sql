ALTER TABLE study_material_sets
    ADD COLUMN teacher_subject_id VARCHAR(36) NULL,
    ADD CONSTRAINT fk_study_material_teacher_subject
        FOREIGN KEY (teacher_subject_id) REFERENCES teacher_subjects (id)
        ON DELETE SET NULL;

CREATE INDEX idx_study_material_sets_teacher_subject ON study_material_sets (teacher_subject_id);
