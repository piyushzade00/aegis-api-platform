ALTER TABLE usage_log
ADD COLUMN event_id VARCHAR(36);

UPDATE usage_log
SET event_id = UUID()
WHERE event_id IS NULL;

ALTER TABLE usage_log
MODIFY event_id VARCHAR(36) NOT NULL;

ALTER TABLE usage_log
ADD CONSTRAINT uk_usage_event_id UNIQUE (event_id);