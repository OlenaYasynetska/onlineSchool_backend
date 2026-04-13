-- Внутрішні нотатки суперадміна про шкільного адміністратора («щоб не забути»).

ALTER TABLE users
  ADD COLUMN super_admin_notes TEXT NULL
  AFTER phone;
