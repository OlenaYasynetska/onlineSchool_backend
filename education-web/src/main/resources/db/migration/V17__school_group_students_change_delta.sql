-- Зміна для журналу активності вчителя (join = +1; наприклад, leave у майбутньому = -1).
ALTER TABLE school_group_students
  ADD COLUMN change_delta INT NOT NULL DEFAULT 1
  COMMENT 'Activity log delta: +1 when student joins group';
