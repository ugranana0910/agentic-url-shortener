package com.navya.agentic_url_shortener.url.service;

import com.navya.agentic_url_shortener.url.exception.InvalidUrlException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

@Component
public class OriginalUrlValidator {

    private static final int MAX_URL_LENGTH = 2048;

    private static final Set<String> ALLOWED_SCHEMES =
            Set.of("http", "https");

    public String validateAndNormalize(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            throw new InvalidUrlException(
                    "URL must not be blank"
            );
        }

        String normalized = candidate.trim();

        if (normalized.length() > MAX_URL_LENGTH) {
            throw new InvalidUrlException(
                    "URL must not exceed 2048 characters"
            );
        }

        try {
            URI uri = new URI(normalized);
            validateScheme(uri);
            validateHost(uri);
            validateUserInfo(uri);

            return uri.normalize().toASCIIString();
        } catch (URISyntaxException exception) {
            throw new InvalidUrlException(
                    "URL syntax is invalid"
            );
        }
    }

    private void validateScheme(URI uri) {
        String scheme = uri.getScheme();

        if (scheme == null ||
                !ALLOWED_SCHEMES.contains(
                        scheme.toLowerCase(Locale.ROOT)
                )) {
            throw new InvalidUrlException(
                    "Only HTTP and HTTPS URLs are supported"
            );
        }
    }

    private void validateHost(URI uri) {
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new InvalidUrlException(
                    "URL must contain a valid host"
            );
        }
    }

    private void validateUserInfo(URI uri) {
        if (uri.getUserInfo() != null) {
            throw new InvalidUrlException(
                    "URLs containing user information are not supported"
            );
        }
    }
}