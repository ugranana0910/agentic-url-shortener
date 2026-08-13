package com.navya.agentic_url_shortener.idempotency.exception;

public class IdempotencyConflictException
        extends RuntimeException {

    public IdempotencyConflictException() {
        super(
                "Idempotency key was already used with a different request"
        );
    }
}