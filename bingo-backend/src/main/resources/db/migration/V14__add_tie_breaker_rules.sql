-- Add tie_breaker_rule to rounds
ALTER TABLE rounds ADD COLUMN tie_breaker_rule VARCHAR(50) DEFAULT 'SPLIT_PRIZE';

-- Add divisible flag to prizes
ALTER TABLE prizes ADD COLUMN divisible BOOLEAN DEFAULT TRUE;
