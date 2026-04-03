-- Align legacy/manual MySQL schemas with backend requirements for school registration.
-- Safe for existing databases: only ADD COLUMN / CREATE INDEX / UPSERT-like inserts.

CREATE TABLE IF NOT EXISTS subscription_plans (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  plan_key VARCHAR(32) NOT NULL UNIQUE,
  title VARCHAR(64) NOT NULL,
  subtitle VARCHAR(128) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO subscription_plans (plan_key, title, subtitle)
VALUES
  ('free', 'Free Plan', '(Forever Free)'),
  ('standard', 'Standard Plan', '(for small schools)'),
  ('pro', 'Pro Plan', '(for growing schools)')
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  subtitle = VALUES(subtitle);

-- Users/organizations alignment is handled by V2 in this repository.
-- Keeping V3 compatible with MySQL versions where "ADD COLUMN IF NOT EXISTS"
-- and "CREATE INDEX IF NOT EXISTS" are not supported.

-- If plan_id is null, set FREE plan as default.
UPDATE organizations o
JOIN subscription_plans p ON p.plan_key = 'free'
SET o.plan_id = p.id
WHERE o.plan_id IS NULL;

-- Add FK only if absent.
SET @has_fk_org_plan :=
  (SELECT COUNT(*)
   FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = DATABASE()
     AND TABLE_NAME = 'organizations'
     AND CONSTRAINT_NAME = 'fk_org_plan'
     AND CONSTRAINT_TYPE = 'FOREIGN KEY');

SET @sql_org_plan_fk := IF(
  @has_fk_org_plan = 0,
  'ALTER TABLE organizations ADD CONSTRAINT fk_org_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans(id)',
  'SELECT 1'
);
PREPARE stmt_org_plan_fk FROM @sql_org_plan_fk;
EXECUTE stmt_org_plan_fk;
DEALLOCATE PREPARE stmt_org_plan_fk;

