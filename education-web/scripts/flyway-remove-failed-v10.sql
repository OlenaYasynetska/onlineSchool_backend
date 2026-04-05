-- Якщо Spring не стартує: "Detected failed migration to version 10 (schedule and homework)".
--
-- Варіант A (рекомендовано): у application.yml вже є spring.flyway.repair-on-migrate: true —
-- просто перезапустіть застосунок, Flyway сам прибере failed-запис.
--
-- Варіант B: виконайте вручну в MySQL (ім'я БД за потреби змініть — у application.yml часто schools):

USE schools;

DELETE FROM flyway_schema_history
WHERE version = '10'
  AND success = 0;

-- Якщо колонки success немає (дуже старий Flyway) або DELETE не змінив рядки:
-- DELETE FROM flyway_schema_history WHERE version = '10';
-- Увага: такий DELETE видалить і успішний V10 — тоді V10 виконається знову; переконайтеся,
-- що V10__schedule_and_homework.sql у проєкті актуальний (умовні CREATE INDEX через information_schema).
