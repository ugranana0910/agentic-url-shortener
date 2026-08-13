package com.navya.agentic_url_shortener.url.exception;

public class ShortUrlUnavailableException extends RuntimeException {

    public ShortUrlUnavailableException(String message) {
        super(message);
    }
}