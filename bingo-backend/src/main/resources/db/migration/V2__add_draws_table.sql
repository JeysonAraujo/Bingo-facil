CREATE TABLE draws (
    id UUID PRIMARY KEY,
    round_id UUID REFERENCES rounds(id) ON DELETE CASCADE NOT NULL,
    ball_number INT NOT NULL,
    drawn_sequence INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_round_ball UNIQUE(round_id, ball_number),
    CONSTRAINT uq_round_sequence UNIQUE(round_id, drawn_sequence)
);

CREATE INDEX idx_draws_round_id ON draws(round_id);
