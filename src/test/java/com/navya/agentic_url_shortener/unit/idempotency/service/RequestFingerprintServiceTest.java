package com.navya.agentic_url_shortener.unit.idempotency.service;

import com.navya.agentic_url_shortener.idempotency.service.RequestFingerprintService;
import com.navya.agentic_url_shortener.url.dto.CreateShortUrlRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RequestFingerprintServiceTest {

    private final RequestFingerprintService service =
            new RequestFingerprintService();

    @Test
    void identicalRequestsProduceSameFingerprint() {
        var first = new CreateShortUrlRequest(
                "https://example.com",
                Instant.parse("2027-08-13T12:00:00Z")
        );

        var second = new CreateShortUrlRequest(
                "https://example.com",
                Instant.parse("2027-08-13T12:00:00Z")
        );

        assertThat(service.fingerprint(first))
                .isEqualTo(service.fingerprint(second))
                .hasSize(64);
    }

    @Test
    void differentUrlsProduceDifferentFingerprints() {
        var first = new CreateShortUrlRequest(
                "https://example.com/one",
                null
        );

        var second = new CreateShortUrlRequest(
                "https://example.com/two",
                null
        );

        assertThat(service.fingerprint(first))
                .isNotEqualTo(service.fingerprint(second));
    }

    @Test
    void expirationParticipatesInFingerprint() {
        var first = new CreateShortUrlRequest(
                "https://example.com",
                Instant.parse("2027-08-13T12:00:00Z")
        );

        var second = new CreateShortUrlRequest(
                "https://example.com",
                Instant.parse("2027-08-14T12:00:00Z")
        );

        assertThat(service.fingerprint(first))
                .isNotEqualTo(service.fingerprint(second));
    }
}