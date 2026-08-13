CREATE TABLE idempotency_records (
    idempotency_key VARCHAR(128) PRIMARY KEY,
    request_hash VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    resource_id UUID,
    response_status INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_idempotency_resource
        FOREIGN KEY (resource_id)
        REFERENCES short_urls (id),

    CONSTRAINT chk_idempotency_status
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'FAILED')),

    CONSTRAINT chk_completed_idempotency_response
        CHECK (
            status <> 'COMPLETED'
            OR (
                resource_id IS NOT NULL
                AND response_status IS NOT NULL
            )
        )
);

CREATE INDEX idx_idempotency_records_expires_at
    ON idempotency_records (expires_at);