package com.navya.agentic_url_shortener.orchestration.exception;

public class TaskExecutionException
        extends RuntimeException {

    public TaskExecutionException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}