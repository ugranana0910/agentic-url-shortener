package com.navya.agentic_url_shortener.idempotency.service;

import com.navya.agentic_url_shortener.url.dto.CreateShortUrlRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class RequestFingerprintService {

    public String fingerprint(
            CreateShortUrlRequest request
    ) {
        String normalizedUrl = request.getUrl() == null
                ? ""
                : request.getUrl().trim();

        Instant expiresAt = request.getExpiresAt();

        String canonicalRequest =
                "url-length=" + normalizedUrl.length()
                        + "\nurl=" + normalizedUrl
                        + "\nexpiresAt="
                        + (expiresAt == null
                        ? ""
                        : expiresAt.toString());

        return sha256(canonicalRequest);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }
}