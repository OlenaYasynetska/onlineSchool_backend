-- Austrian scale (1–5) in addition to stars (1–3).
ALTER TABLE homework_portal_submissions
    DROP CHECK chk_hw_portal_stars;

ALTER TABLE homework_portal_submissions
    ADD CONSTRAINT chk_hw_portal_stars CHECK (stars IS NULL OR stars BETWEEN 1 AND 5);

ALTER TABLE homework_portal_submissions
    MODIFY COLUMN stars INT NULL COMMENT '1-3 stars or 1-5 Austrian grade after review';
