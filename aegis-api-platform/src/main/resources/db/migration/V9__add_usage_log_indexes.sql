CREATE INDEX idx_usage_tenant ON usage_log(tenant_id);

CREATE INDEX idx_usage_api ON usage_log(api_id);

CREATE INDEX idx_usage_tenant_created
ON usage_log(tenant_id, created_at);

CREATE INDEX idx_usage_api_created
ON usage_log(api_id, created_at);