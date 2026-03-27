-- Админские таблицы (MySQL) под UI суперадмина: планы, организации и платежи.
-- Базовые таблицы для авторизации и связей с существующей таблицей students.

CREATE TABLE IF NOT EXISTS subscription_plans (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  plan_key VARCHAR(32) NOT NULL UNIQUE, -- free|standard|pro
  title VARCHAR(64) NOT NULL,
  subtitle VARCHAR(128) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS users (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  role VARCHAR(32) NOT NULL, -- SUPER_ADMIN|ADMIN_SCHOOL|TEACHER|STUDENT
  enabled BOOLEAN NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS organizations (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(500) NULL,

  admin_user_id VARCHAR(36) NOT NULL, -- user.id (ADMIN_SCHOOL)

  plan_id INT NOT NULL,
  payment_period VARCHAR(16) NOT NULL, -- monthly|yearly|quarterly
  status VARCHAR(32) NOT NULL, -- Active|Expiring soon|Inactive
  next_billing_at DATETIME NULL,

  registered_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  total_received DECIMAL(12,2) NOT NULL DEFAULT 0,

  address VARCHAR(255) NULL,
  country VARCHAR(64) NULL,

  CONSTRAINT fk_org_plan
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX ix_org_plan_id ON organizations(plan_id);
CREATE INDEX ix_org_next_billing_at ON organizations(next_billing_at);
CREATE INDEX ix_org_admin_user_id ON organizations(admin_user_id);

CREATE TABLE IF NOT EXISTS payments (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  organization_id VARCHAR(36) NOT NULL,

  amount DECIMAL(12,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'USD',

  status VARCHAR(32) NOT NULL, -- Paid|Pending payment|Failed
  paid_at TIMESTAMP(6) NULL,

  invoice_number VARCHAR(64) NULL,
  invoice_url VARCHAR(512) NULL,

  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_payment_org
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX ix_payments_org_id ON payments(organization_id);
CREATE INDEX ix_payments_paid_at ON payments(paid_at);

CREATE TABLE IF NOT EXISTS teachers (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL UNIQUE,
  school_id VARCHAR(36) NOT NULL,
  subject VARCHAR(255) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

  CONSTRAINT fk_teacher_user
    FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_teacher_school
    FOREIGN KEY (school_id) REFERENCES organizations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Привязка существующей таблицы students к organizations
ALTER TABLE students
  ADD CONSTRAINT fk_students_school
    FOREIGN KEY (school_id) REFERENCES organizations(id);

