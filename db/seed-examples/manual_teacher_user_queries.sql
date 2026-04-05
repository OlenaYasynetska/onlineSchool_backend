-- Запускайте в Workbench після USE <ваша_база>; (наприклад schools або school_db)
-- Модалка "Add teacher" заповнює таблиці users і teachers; предмети — teacher_subjects (+ school_subjects).

-- 1) Останні викладачі з іменем, email (пароль лише як hash у users.password_hash)
SELECT
  t.id AS teacher_id,
  u.email,
  u.first_name,
  u.last_name,
  u.password_hash,
  t.school_id,
  t.created_at
FROM teachers t
JOIN users u ON u.id = t.user_id
ORDER BY t.created_at DESC
LIMIT 20;

-- 2) Предмети викладача
SELECT ts.title, ts.sort_order, ts.created_at
FROM teacher_subjects ts
WHERE ts.teacher_id = 'ВСТАВТЕ_UUID_з_teachers.id'
ORDER BY ts.sort_order;

-- 3) Якщо застосовано міграцію V8 — можна SELECT з view:
-- SELECT * FROM v_teacher_accounts ORDER BY teacher_created_at DESC;
