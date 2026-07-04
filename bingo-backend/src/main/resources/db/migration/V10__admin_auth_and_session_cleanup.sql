CREATE TABLE admins (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    reset_code VARCHAR(6),
    reset_code_expires_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE admin_sessions (
    id UUID PRIMARY KEY,
    admin_id UUID REFERENCES admins(id) ON DELETE CASCADE NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE events ADD COLUMN admin_id UUID REFERENCES admins(id) ON DELETE SET NULL;

CREATE INDEX idx_admin_sessions_token ON admin_sessions(token);
CREATE INDEX idx_events_admin_id ON events(admin_id);
