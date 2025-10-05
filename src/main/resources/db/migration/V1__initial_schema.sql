CREATE TABLE IF NOT EXISTS activity_records (
    event_id VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    repository_name VARCHAR(500) NOT NULL,
    description TEXT,
    event_time TIMESTAMP NOT NULL,
    fetched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    raw_payload TEXT,
    CONSTRAINT pk_activity_records PRIMARY KEY (event_id)
);

CREATE INDEX idx_activity_username ON activity_records(username);
CREATE INDEX idx_activity_event_type ON activity_records(event_type);
CREATE INDEX idx_activity_event_time ON activity_records(event_time DESC);
CREATE INDEX idx_activity_username_event_type ON activity_records(username, event_type);
CREATE INDEX idx_activity_username_event_time ON activity_records(username, event_time DESC);

COMMENT ON TABLE activity_records IS 'Stores GitHub activity events fetched from the API';
COMMENT ON COLUMN activity_records.event_id IS 'Unique identifier from GitHub API';
COMMENT ON COLUMN activity_records.username IS 'GitHub username';
COMMENT ON COLUMN activity_records.event_type IS 'Type of GitHub event (PushEvent, CreateEvent, etc.)';
COMMENT ON COLUMN activity_records.repository_name IS 'Full repository name (owner/repo)';
COMMENT ON COLUMN activity_records.description IS 'Human-readable description of the activity';
COMMENT ON COLUMN activity_records.event_time IS 'When the event occurred on GitHub';
COMMENT ON COLUMN activity_records.fetched_at IS 'When we fetched this data from GitHub API';
COMMENT ON COLUMN activity_records.raw_payload IS 'JSON payload from GitHub API';