-- Телефон користувача (опційно); для відображення в кабінеті адміністратора школи.

ALTER TABLE users
  ADD COLUMN phone VARCHAR(32) NULL
  AFTER last_name;
