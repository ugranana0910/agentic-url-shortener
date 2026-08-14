package com.navya.agentic_url_shortener.tool.repository;

public class RepositoryAccessException
        extends RuntimeException {

    public RepositoryAccessException(String message) {
        super(message);
    }

    public RepositoryAccessException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}