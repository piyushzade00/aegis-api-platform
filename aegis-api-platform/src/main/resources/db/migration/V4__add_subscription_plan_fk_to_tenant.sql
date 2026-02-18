ALTER TABLE tenant
ADD COLUMN subscription_plan_id BIGINT NOT NULL;

ALTER TABLE tenant
ADD CONSTRAINT fk_tenant_subscription_plan
FOREIGN KEY (subscription_plan_id)
REFERENCES subscription_plan(id);
