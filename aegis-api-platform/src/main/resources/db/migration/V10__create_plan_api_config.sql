CREATE TABLE plan_api_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    api_id BIGINT NOT NULL,
    rate_limit_per_minute INT NULL,
    monthly_quota_override BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_api_plan
        FOREIGN KEY (plan_id)
        REFERENCES subscription_plan(id),
    CONSTRAINT fk_plan_api_api
        FOREIGN KEY (api_id)
        REFERENCES api_definition(id),
    CONSTRAINT uk_plan_api UNIQUE (plan_id, api_id)
);