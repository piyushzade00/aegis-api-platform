CREATE TABLE usage_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    api_id BIGINT NOT NULL,
    api_key_id BIGINT NOT NULL,
    status_code INT NOT NULL,
    latency_ms BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
