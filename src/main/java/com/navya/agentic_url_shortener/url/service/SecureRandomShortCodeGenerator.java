package com.navya.agentic_url_shortener.url.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureRandomShortCodeGenerator
        implements ShortCodeGenerator {

    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
                    .toCharArray();

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate(int length) {
        if (length < 6 || length > 32) {
            throw new IllegalArgumentException(
                    "Short-code length must be between 6 and 32"
            );
        }

        char[] result = new char[length];

        for (int index = 0; index < length; index++) {
            int alphabetIndex =
                    secureRandom.nextInt(ALPHABET.length);

            result[index] = ALPHABET[alphabetIndex];
        }

        return new String(result);
    }
}