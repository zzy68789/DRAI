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

CREATE TABLE IF NOT EXISTS research_task (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  owner_id BIGINT NOT NULL DEFAULT 0,
  thread_id VARCHAR(64) NOT NULL,
  query LONGTEXT NOT NULL,
  search_mode VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  revision_number INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_research_task_owner_id (owner_id),
  INDEX idx_research_task_thread_id (thread_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agent_step_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  task_id BIGINT NOT NULL,
  step_name VARCHAR(64) NOT NULL,
  input_snapshot LONGTEXT,
  output_snapshot LONGTEXT,
  status VARCHAR(32) NOT NULL,
  error_message LONGTEXT,
  created_at DATETIME NOT NULL,
  INDEX idx_agent_step_log_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS report (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  owner_id BIGINT NOT NULL DEFAULT 0,
  task_id BIGINT,
  thread_id VARCHAR(64) NOT NULL,
  content LONGTEXT NOT NULL,
  version INT NOT NULL DEFAULT 1,
  review_status VARCHAR(32),
  critique LONGTEXT,
  favorite TINYINT(1) NOT NULL DEFAULT 0,
  indexed_at DATETIME,
  deleted_at DATETIME,
  created_at DATETIME NOT NULL,
  INDEX idx_report_owner_id (owner_id),
  INDEX idx_report_thread_id (thread_id),
  INDEX idx_report_favorite (favorite),
  INDEX idx_report_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS checkpoint (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  thread_id VARCHAR(64) NOT NULL,
  task_id BIGINT,
  state_json LONGTEXT NOT NULL,
  created_at DATETIME NOT NULL,
  INDEX idx_checkpoint_thread_id (thread_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
