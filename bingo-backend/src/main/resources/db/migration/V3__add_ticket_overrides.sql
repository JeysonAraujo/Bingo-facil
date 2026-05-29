ALTER TABLE tickets ADD COLUMN marked_overrides JSONB DEFAULT '[]'::jsonb NOT NULL;
