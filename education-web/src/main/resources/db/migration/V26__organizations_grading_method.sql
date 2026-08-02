ALTER TABLE organizations
    ADD COLUMN grading_method VARCHAR(16) NOT NULL DEFAULT 'sum';
