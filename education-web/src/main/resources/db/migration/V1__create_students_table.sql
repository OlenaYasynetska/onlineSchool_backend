CREATE TABLE IF NOT EXISTS students (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    school_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE UNIQUE INDEX ux_students_email ON students (email);
CREATE INDEX ix_students_school_id ON students (school_id);

