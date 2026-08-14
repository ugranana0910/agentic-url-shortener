package com.navya.agentic_url_shortener.tool.build;

public class BuildExecutionException
        extends RuntimeException {

    public BuildExecutionException(String message) {
        super(message);
    }

    public BuildExecutionException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}