package com.navya.agentic_url_shortener.url.service;

import com.navya.agentic_url_shortener.config.UrlShortenerProperties;
import com.navya.agentic_url_shortener.url.domain.ShortUrl;
import com.navya.agentic_url_shortener.url.dto.CreateShortUrlRequest;
import com.navya.agentic_url_shortener.url.dto.RedirectTarget;
import com.navya.agentic_url_shortener.url.dto.ShortUrlResponse;
import com.navya.agentic_url_shortener.url.exception.ShortCodeGenerationException;
import com.navya.agentic_url_shortener.url.exception.ShortUrlNotFoundException;
import com.navya.agentic_url_shortener.url.exception.ShortUrlUnavailableException;
import com.navya.agentic_url_shortener.url.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShortUrlService {

    private final ShortUrlRepository repository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final OriginalUrlValidator originalUrlValidator;
    private final UrlShortenerProperties properties;
    private final Clock clock;

    @Transactional
    public ShortUrlResponse create(
            CreateShortUrlRequest request
    ) {
        String originalUrl =
                originalUrlValidator.validateAndNormalize(
                        request.getUrl()
                );

        Instant now = clock.instant();
        validateExpiration(request.getExpiresAt(), now);

        String shortCode = allocateShortCode();

        ShortUrl entity = new ShortUrl(
                UUID.randomUUID(),
                shortCode,
                originalUrl,
                now,
                request.getExpiresAt()
        );

        ShortUrl saved = repository.save(entity);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ShortUrlResponse get(String shortCode) {
        return toResponse(findByShortCode(shortCode));
    }

    @Transactional(readOnly = true)
    public RedirectTarget resolve(String shortCode) {
        ShortUrl shortUrl = findByShortCode(shortCode);
        Instant now = clock.instant();

        if (shortUrl.isExpiredAt(now)) {
            throw new ShortUrlUnavailableException(
                    "Short URL has expired"
            );
        }

        if (!shortUrl.isActiveAt(now)) {
            throw new ShortUrlUnavailableException(
                    "Short URL is disabled"
            );
        }

        return new RedirectTarget(shortUrl.getOriginalUrl());
    }

    private void validateExpiration(
            Instant expiresAt,
            Instant now
    ) {
        if (expiresAt != null && !expiresAt.isAfter(now)) {
            throw new IllegalArgumentException(
                    "expiresAt must be in the future"
            );
        }
    }

    private ShortUrl findByShortCode(String shortCode) {
        if (shortCode == null || shortCode.isBlank()) {
            throw new ShortUrlNotFoundException("");
        }

        return repository.findByShortCode(shortCode)
                .orElseThrow(
                        () -> new ShortUrlNotFoundException(shortCode)
                );
    }

    private String allocateShortCode() {
        for (
                int attempt = 0;
                attempt < properties.getGenerationAttempts();
                attempt++
        ) {
            String candidate = shortCodeGenerator.generate(
                    properties.getCodeLength()
            );

            if (!repository.existsByShortCode(candidate)) {
                return candidate;
            }
        }

        throw new ShortCodeGenerationException();
    }

    private ShortUrlResponse toResponse(ShortUrl shortUrl) {
        String baseUrl = removeTrailingSlash(
                properties.getBaseUrl()
        );

        return ShortUrlResponse.from(
                shortUrl,
                baseUrl + "/" + shortUrl.getShortCode()
        );
    }

    private String removeTrailingSlash(String value) {
        String result = value;

        while (result.endsWith("/")) {
            result = result.substring(
                    0,
                    result.length() - 1
            );
        }

        return result;
    }
}