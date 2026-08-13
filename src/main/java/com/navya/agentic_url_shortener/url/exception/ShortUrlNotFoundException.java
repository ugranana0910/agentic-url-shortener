package com.navya.agentic_url_shortener.url.exception;

public class ShortUrlNotFoundException extends RuntimeException {

    public ShortUrlNotFoundException(String shortCode) {
        super("Short URL not found: " + shortCode);
    }
}