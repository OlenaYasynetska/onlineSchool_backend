-- Сумарні зірки за ДЗ усіх учнів групи (накопичується при оцінюванні викладачем).
ALTER TABLE school_groups
ADD COLUMN homework_stars_total INT NOT NULL DEFAULT 0 AFTER students_count;
