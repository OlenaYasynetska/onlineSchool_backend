-- Приклади для MySQL Workbench (після того, як Flyway застосував міграції V1–V4).
-- Нічого звідси автоматично не виконується — скопіюйте потрібні блоки вручну.

-- 1) Дата доступу в сайдбарі (поле "Access until") береться з organizations.next_billing_at
--    Якщо там NULL — у UI буде "—".
-- UPDATE organizations
-- SET next_billing_at = '2026-12-31 23:59:59'
-- WHERE id = 'ВАШ_ORGANIZATION_ID';

-- 2) Додати групу для школи (organization_id = id організації з таблиці organizations)
/*
INSERT INTO school_groups (
  id,
  organization_id,
  name,
  code,
  topics_label,
  start_date,
  end_date,
  students_count,
  active,
  created_at
) VALUES (
  UUID(),
  'ВАШ_ORGANIZATION_ID',
  'Morning A1',
  'CODE-101',
  'Grammar · Speaking',
  '2026-01-10',
  '2026-06-30',
  14,
  1,
  CURRENT_TIMESTAMP(6)
);
*/

-- Перевірка
-- SELECT * FROM school_groups WHERE organization_id = 'ВАШ_ORGANIZATION_ID';
