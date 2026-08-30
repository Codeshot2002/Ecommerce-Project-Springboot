CREATE TABLE IF NOT EXISTS category (
    id BIGINT NOT NULL AUTO_INCREMENT, name VARCHAR(100) NOT NULL, description VARCHAR(1000), version BIGINT, PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS product (
    id BIGINT NOT NULL AUTO_INCREMENT, name VARCHAR(100), description VARCHAR(1000), quantity BIGINT, category_id BIGINT, version BIGINT, PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT, email VARCHAR(254) NOT NULL, display_name VARCHAR(120), password_hash VARCHAR(100), role VARCHAR(20) NOT NULL,
    token_version BIGINT NOT NULL, enabled BIT NOT NULL, PRIMARY KEY (id), CONSTRAINT uk_users_email UNIQUE (email)
);
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT NOT NULL AUTO_INCREMENT, status VARCHAR(20) NOT NULL, PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, product_id BIGINT NOT NULL, quantity BIGINT NOT NULL, PRIMARY KEY (id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id), CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(id)
);
CREATE TABLE IF NOT EXISTS oauth_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT, user_id BIGINT NOT NULL, provider VARCHAR(30) NOT NULL, provider_subject VARCHAR(255) NOT NULL, PRIMARY KEY (id),
    CONSTRAINT uk_oauth_provider_subject UNIQUE (provider, provider_subject), CONSTRAINT fk_oauth_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT, user_id BIGINT NOT NULL, token_hash VARCHAR(64) NOT NULL, family_id VARCHAR(36) NOT NULL, expires_at TIMESTAMP NOT NULL,
    revoked BIT NOT NULL, device_info VARCHAR(255), PRIMARY KEY (id), CONSTRAINT uk_refresh_hash UNIQUE (token_hash), CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id)
);
