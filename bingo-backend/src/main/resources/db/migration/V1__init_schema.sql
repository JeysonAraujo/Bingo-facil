CREATE TABLE events (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE rounds (
    id UUID PRIMARY KEY,
    event_id UUID REFERENCES events(id) ON DELETE CASCADE NOT NULL,
    round_number INT NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE prizes (
    id UUID PRIMARY KEY,
    round_id UUID REFERENCES rounds(id) ON DELETE CASCADE NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    draw_pattern VARCHAR(100) NOT NULL
);

CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    event_id UUID REFERENCES events(id) ON DELETE CASCADE NOT NULL,
    serial VARCHAR(50) NOT NULL UNIQUE,
    matrix JSONB NOT NULL,
    activated BOOLEAN DEFAULT FALSE NOT NULL,
    session_token VARCHAR(255)
);

CREATE INDEX idx_events_created_at ON events(created_at);
CREATE INDEX idx_rounds_event_id ON rounds(event_id);
CREATE INDEX idx_prizes_round_id ON prizes(round_id);
CREATE INDEX idx_tickets_event_id ON tickets(event_id);
CREATE INDEX idx_tickets_serial ON tickets(serial);
