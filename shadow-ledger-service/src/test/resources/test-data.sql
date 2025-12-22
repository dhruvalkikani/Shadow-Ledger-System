DROP TABLE IF EXISTS ledger_entries;

-- Create table with quoted "TYPE" to avoid H2 reserved word conflict
CREATE TABLE ledger_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL UNIQUE,
    account_id VARCHAR(255) NOT NULL,
    "TYPE" VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    event_timestamp BIGINT NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_correction BOOLEAN DEFAULT FALSE
);

-- Insert test data
INSERT INTO ledger_entries (event_id, account_id, "TYPE", amount, event_timestamp) VALUES
('E001', 'A10', 'CREDIT', 1000.00, 1702540800000),
('E002', 'A10', 'DEBIT', 250.00, 1702540860000),
('E003', 'A11', 'CREDIT', 500.00, 1702540920000),
('E004', 'A11', 'DEBIT', 200.00, 1702540980000),
('E005', 'A10', 'CREDIT', 0.00, 1702541040000);