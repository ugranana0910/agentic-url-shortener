package com.navya.agentic_url_shortener.url.exception;

public class ShortCodeGenerationException extends RuntimeException {

    public ShortCodeGenerationException() {
        super("Unable to allocate a unique short code");
    }
}