CREATE TABLE IF NOT EXISTS app_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL,
  email VARCHAR(128),
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(32) NOT NULL DEFAULT 'USER',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_app_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE research_task
  ADD COLUMN owner_id BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_research_task_owner_id ON research_task(owner_id);

ALTER TABLE report
  ADD COLUMN owner_id BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_report_owner_id ON report(owner_id);
