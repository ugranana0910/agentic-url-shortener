package com.navya.agentic_url_shortener.unit.url.service;

import com.navya.agentic_url_shortener.config.UrlShortenerProperties;
import com.navya.agentic_url_shortener.url.domain.ShortUrl;
import com.navya.agentic_url_shortener.url.dto.CreateShortUrlRequest;
import com.navya.agentic_url_shortener.url.exception.ShortCodeGenerationException;
import com.navya.agentic_url_shortener.url.exception.ShortUrlNotFoundException;
import com.navya.agentic_url_shortener.url.exception.ShortUrlUnavailableException;
import com.navya.agentic_url_shortener.url.repository.ShortUrlRepository;
import com.navya.agentic_url_shortener.url.service.OriginalUrlValidator;
import com.navya.agentic_url_shortener.url.service.ShortCodeGenerator;
import com.navya.agentic_url_shortener.url.service.ShortUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShortUrlServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-08-13T12:00:00Z");

    private ShortUrlRepository repository;
    private ShortCodeGenerator generator;
    private ShortUrlService service;

    @BeforeEach
    void setUp() {
        repository = mock(ShortUrlRepository.class);
        generator = mock(ShortCodeGenerator.class);

        UrlShortenerProperties properties =
                new UrlShortenerProperties();

        properties.setBaseUrl("http://localhost:8080");
        properties.setCodeLength(8);
        properties.setGenerationAttempts(3);

        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        service = new ShortUrlService(
                repository,
                generator,
                new OriginalUrlValidator(),
                properties,
                clock
        );
    }

    @Test
    void createsShortUrl() {
        when(generator.generate(8))
                .thenReturn("Ab12Cd34");

        when(repository.existsByShortCode("Ab12Cd34"))
                .thenReturn(false);

        when(repository.save(any(ShortUrl.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );

        CreateShortUrlRequest request =
                new CreateShortUrlRequest(
                        "https://example.com/products",
                        NOW.plusSeconds(3600)
                );

        var response = service.create(request);

        assertThat(response.getShortCode())
                .isEqualTo("Ab12Cd34");

        assertThat(response.getShortUrl())
                .isEqualTo(
                        "http://localhost:8080/Ab12Cd34"
                );

        assertThat(response.getOriginalUrl())
                .isEqualTo(
                        "https://example.com/products"
                );

        assertThat(response.getCreatedAt())
                .isEqualTo(NOW);

        verify(repository).save(any(ShortUrl.class));
    }

    @Test
    void retriesGeneratedCodeCollision() {
        when(generator.generate(8))
                .thenReturn("Existing")
                .thenReturn("Unique12");

        when(repository.existsByShortCode("Existing"))
                .thenReturn(true);

        when(repository.existsByShortCode("Unique12"))
                .thenReturn(false);

        when(repository.save(any(ShortUrl.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );

        var response = service.create(
                new CreateShortUrlRequest(
                        "https://example.com",
                        null
                )
        );

        assertThat(response.getShortCode())
                .isEqualTo("Unique12");
    }

    @Test
    void failsAfterCollisionAttemptLimit() {
        when(generator.generate(8))
                .thenReturn("Existing");

        when(repository.existsByShortCode("Existing"))
                .thenReturn(true);

        assertThatThrownBy(
                () -> service.create(
                        new CreateShortUrlRequest(
                                "https://example.com",
                                null
                        )
                )
        )
                .isInstanceOf(
                        ShortCodeGenerationException.class
                )
                .hasMessage(
                        "Unable to allocate a unique short code"
                );

        verify(repository, never()).save(any());
    }

    @Test
    void resolvesActiveUrl() {
        ShortUrl shortUrl = new ShortUrl(
                UUID.randomUUID(),
                "Ab12Cd34",
                "https://example.com",
                NOW.minusSeconds(60),
                null
        );

        when(repository.findByShortCode("Ab12Cd34"))
                .thenReturn(Optional.of(shortUrl));

        var target = service.resolve("Ab12Cd34");

        assertThat(target.getOriginalUrl())
                .isEqualTo("https://example.com");
    }

    @Test
    void rejectsExpiredUrl() {
        ShortUrl shortUrl = new ShortUrl(
                UUID.randomUUID(),
                "Ab12Cd34",
                "https://example.com",
                NOW.minusSeconds(3600),
                NOW.minusSeconds(1)
        );

        when(repository.findByShortCode("Ab12Cd34"))
                .thenReturn(Optional.of(shortUrl));

        assertThatThrownBy(
                () -> service.resolve("Ab12Cd34")
        )
                .isInstanceOf(
                        ShortUrlUnavailableException.class
                )
                .hasMessage("Short URL has expired");
    }

    @Test
    void rejectsUnknownCode() {
        when(repository.findByShortCode("Missing1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.resolve("Missing1")
        )
                .isInstanceOf(
                        ShortUrlNotFoundException.class
                )
                .hasMessage(
                        "Short URL not found: Missing1"
                );
    }
}