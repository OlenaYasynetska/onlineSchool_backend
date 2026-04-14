-- Чи показувати рядок предмету/програми на картці групи (окремо від збережених даних у БД).
ALTER TABLE school_groups
  ADD COLUMN show_subject_on_card TINYINT(1) NOT NULL DEFAULT 1
  AFTER topics_label;
