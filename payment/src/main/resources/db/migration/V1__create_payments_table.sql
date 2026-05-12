CREATE TABLE payments (
      id UUID PRIMARY KEY,
      order_id VARCHAR(100) NOT NULL,
      user_id VARCHAR(100) NOT NULL,
      amount NUMERIC(12, 2) NOT NULL,
      status VARCHAR(50) NOT NULL,
      created_at TIMESTAMP NOT NULL
);