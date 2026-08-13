package com.navya.agentic_url_shortener.unit.url.domain;

import com.navya.agentic_url_shortener.url.domain.ShortUrl;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShortUrlTest {

    private static final Instant NOW =
            Instant.parse("2026-08-13T12:00:00Z");

    @Test
    void activeUrlWithoutExpirationIsAvailable() {
        ShortUrl shortUrl = createShortUrl(null);

        assertThat(
                shortUrl.isActiveAt(NOW.plusSeconds(3600))
        ).isTrue();
    }

    @Test
    void expiredUrlIsUnavailable() {
        ShortUrl shortUrl = createShortUrl(
                NOW.plusSeconds(60)
        );

        assertThat(
                shortUrl.isExpiredAt(NOW.plusSeconds(60))
        ).isTrue();

        assertThat(
                shortUrl.isActiveAt(NOW.plusSeconds(60))
        ).isFalse();
    }

    @Test
    void disabledUrlIsUnavailable() {
        ShortUrl shortUrl = createShortUrl(null);

        shortUrl.disable();

        assertThat(shortUrl.isActiveAt(NOW)).isFalse();
    }

    private ShortUrl createShortUrl(Instant expiresAt) {
        return new ShortUrl(
                UUID.randomUUID(),
                "Ab12Cd34",
                "https://example.com",
                NOW,
                expiresAt
        );
    }
}