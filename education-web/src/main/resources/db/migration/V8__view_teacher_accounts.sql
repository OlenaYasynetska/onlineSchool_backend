-- Зручне подання: дані з модалки "Add teacher" лежать у `users` + рядок у `teachers`.
-- Пароль у БД лише як hash (BCrypt), ніколи не зберігайте plain text у колонці.

CREATE OR REPLACE VIEW v_teacher_accounts AS
SELECT
  t.id AS teacher_id,
  t.school_id,
  u.id AS user_id,
  u.email,
  u.first_name,
  u.last_name,
  u.password_hash,
  u.role,
  u.enabled,
  t.created_at AS teacher_created_at,
  u.created_at AS user_created_at
FROM teachers t
INNER JOIN users u ON u.id = t.user_id;
