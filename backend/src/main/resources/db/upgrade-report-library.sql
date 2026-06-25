ALTER TABLE report
  ADD COLUMN favorite TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN indexed_at DATETIME NULL,
  ADD COLUMN deleted_at DATETIME NULL;

CREATE INDEX idx_report_favorite ON report(favorite);
CREATE INDEX idx_report_deleted_at ON report(deleted_at);
