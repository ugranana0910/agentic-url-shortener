package com.navya.agentic_url_shortener.url.dto;

import com.navya.agentic_url_shortener.url.domain.ShortUrl;
import com.navya.agentic_url_shortener.url.domain.ShortUrlStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlResponse {

    private UUID id;
    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    private ShortUrlStatus status;
    private Instant createdAt;
    private Instant expiresAt;

    public static ShortUrlResponse from(
            ShortUrl entity,
            String publicShortUrl
    ) {
        return new ShortUrlResponse(
                entity.getId(),
                entity.getShortCode(),
                publicShortUrl,
                entity.getOriginalUrl(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getExpiresAt()
        );
    }
}