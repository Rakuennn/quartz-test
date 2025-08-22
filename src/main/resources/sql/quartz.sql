CREATE TABLE IF NOT EXISTS SCHEDULE_JOBS (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  job_name VARCHAR(255) UNIQUE,
  cron_expression VARCHAR(255),
  job_data TEXT, -- JSON หรือ key-value ก็ได้
  status VARCHAR(50), -- active, paused, etc.
  last_modified TIMESTAMP
);