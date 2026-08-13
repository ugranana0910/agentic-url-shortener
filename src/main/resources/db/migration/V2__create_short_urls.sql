CREATE TABLE short_urls (
    id UUID PRIMARY KEY,
    short_code VARCHAR(32) NOT NULL,
    original_url VARCHAR(2048) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_short_urls_short_code UNIQUE (short_code),

    CONSTRAINT chk_short_urls_status
        CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE INDEX idx_short_urls_status_expires_at
    ON short_urls (status, expires_at);