package com.navya.agentic_url_shortener.idempotency.exception;

public class InvalidIdempotencyKeyException
        extends RuntimeException {

    public InvalidIdempotencyKeyException(String message) {
        super(message);
    }
}