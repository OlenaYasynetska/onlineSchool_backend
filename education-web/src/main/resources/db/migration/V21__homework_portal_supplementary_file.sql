-- Додаткове вкладення до здачі зі статусом submitted (файл +/або текст у модалці учня).
ALTER TABLE homework_portal_submissions
  ADD COLUMN supplementary_file_name VARCHAR(255) NULL
    COMMENT 'другий файл, доданий після першої здачі'
    AFTER file_size_bytes,
  ADD COLUMN supplementary_storage_path VARCHAR(1024) NULL
    AFTER supplementary_file_name,
  ADD COLUMN supplementary_content_type VARCHAR(128) NULL
    AFTER supplementary_storage_path,
  ADD COLUMN supplementary_file_size_bytes BIGINT NULL
    AFTER supplementary_content_type;
