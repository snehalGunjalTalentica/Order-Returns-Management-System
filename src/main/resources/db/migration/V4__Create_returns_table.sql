CREATE TABLE returns (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    return_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    return_reason TEXT NOT NULL,
    refund_amount DECIMAL(10, 2) NOT NULL,
    manager_notes TEXT,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_return_order ON returns(order_id);
CREATE INDEX idx_return_number ON returns(return_number);
CREATE INDEX idx_return_status ON returns(status);



