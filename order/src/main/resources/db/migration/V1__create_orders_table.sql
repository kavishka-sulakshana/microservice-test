CREATE TABLE orders (
    id UUID PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);