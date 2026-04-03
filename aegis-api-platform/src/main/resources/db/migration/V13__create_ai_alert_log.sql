CREATE TABLE ai_alert_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
	alert_id VARCHAR(36) NOT NULL UNIQUE,
    alert_type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alert_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id)
);