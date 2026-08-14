package com.navya.agentic_url_shortener.governance;

public class PolicyViolationException
        extends RuntimeException {

    public PolicyViolationException(String message) {
        super(message);
    }
}