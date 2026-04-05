-- Hibernate maps java.lang.Integer to SQL INTEGER; TINYINT from V14 fails schema validation (ddl-auto: validate).
ALTER TABLE homework_portal_submissions
  MODIFY COLUMN stars INT NULL COMMENT '1-3 після перевірки';
