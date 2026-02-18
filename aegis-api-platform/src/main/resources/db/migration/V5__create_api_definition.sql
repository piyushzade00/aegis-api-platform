CREATE TABLE api_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    path VARCHAR(255) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    target_url VARCHAR(500) NOT NULL,
	description VARCHAR(500),
	is_public BOOLEAN NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_api_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id)
		ON DELETE RESTRICT,

    CONSTRAINT uk_tenant_path_method
        UNIQUE (tenant_id, path, http_method)
);
