-- This migration supports databases created before Flyway was introduced.
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT, email VARCHAR(254) NOT NULL, display_name VARCHAR(120), password_hash VARCHAR(100), role VARCHAR(20) NOT NULL,
    token_version BIGINT NOT NULL, enabled BIT NOT NULL, PRIMARY KEY (id), CONSTRAINT uk_users_email UNIQUE (email)
);
CREATE TABLE IF NOT EXISTS oauth_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT, user_id BIGINT NOT NULL, provider VARCHAR(30) NOT NULL, provider_subject VARCHAR(255) NOT NULL, PRIMARY KEY (id),
    CONSTRAINT uk_oauth_provider_subject UNIQUE (provider, provider_subject), CONSTRAINT fk_oauth_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT, user_id BIGINT NOT NULL, token_hash VARCHAR(64) NOT NULL, family_id VARCHAR(36) NOT NULL, expires_at TIMESTAMP NOT NULL,
    revoked BIT NOT NULL, device_info VARCHAR(255), PRIMARY KEY (id), CONSTRAINT uk_refresh_hash UNIQUE (token_hash), CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id)
);
INSERT IGNORE INTO users (email, display_name, password_hash, role, token_version, enabled)
VALUES ('legacy-orders@local.invalid', 'Legacy orders', NULL, 'ADMIN', 0, true);
DELIMITER $$
CREATE PROCEDURE add_orders_customer_column()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'orders' AND column_name = 'customer_id') THEN
        ALTER TABLE orders ADD COLUMN customer_id BIGINT NULL;
    END IF;
END$$
DELIMITER ;
CALL add_orders_customer_column();
DROP PROCEDURE add_orders_customer_column;
UPDATE orders SET customer_id = (SELECT id FROM users WHERE email = 'legacy-orders@local.invalid') WHERE customer_id IS NULL;
ALTER TABLE orders MODIFY customer_id BIGINT NOT NULL;
DELIMITER $$
CREATE PROCEDURE add_orders_customer_fk()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = DATABASE() AND table_name = 'orders' AND constraint_name = 'fk_orders_customer') THEN
        ALTER TABLE orders ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES users(id);
    END IF;
END$$
DELIMITER ;
CALL add_orders_customer_fk();
DROP PROCEDURE add_orders_customer_fk;
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
