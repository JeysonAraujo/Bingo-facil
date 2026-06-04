CREATE TABLE claims (
    id UUID PRIMARY KEY,
    ticket_id UUID REFERENCES tickets(id) ON DELETE CASCADE NOT NULL,
    round_id UUID REFERENCES rounds(id) ON DELETE CASCADE NOT NULL,
    card_index INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_claims_round_id ON claims(round_id);
