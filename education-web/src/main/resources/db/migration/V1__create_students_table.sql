CREATE TABLE IF NOT EXISTS students (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    school_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL
);

CREATE UNIQUE INDEX ux_students_email ON students (email);
CREATE INDEX ix_students_school_id ON students (school_id);

