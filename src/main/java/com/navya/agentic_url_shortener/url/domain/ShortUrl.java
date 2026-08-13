package com.navya.agentic_url_shortener.url.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "short_urls")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShortUrl {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(
            name = "short_code",
            nullable = false,
            unique = true,
            length = 32,
            updatable = false
    )
    private String shortCode;

    @Column(
            name = "original_url",
            nullable = false,
            length = 2048,
            updatable = false
    )
    private String originalUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShortUrlStatus status;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Version
    @Column(nullable = false)
    private long version;

    public ShortUrl(
            UUID id,
            String shortCode,
            String originalUrl,
            Instant createdAt,
            Instant expiresAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.shortCode = Objects.requireNonNull(shortCode);
        this.originalUrl = Objects.requireNonNull(originalUrl);
        this.status = ShortUrlStatus.ACTIVE;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresAt = expiresAt;
    }

    public boolean isExpiredAt(Instant instant) {
        Objects.requireNonNull(instant);

        return expiresAt != null &&
                !expiresAt.isAfter(instant);
    }

    public boolean isActiveAt(Instant instant) {
        return status == ShortUrlStatus.ACTIVE &&
                !isExpiredAt(instant);
    }

    public void disable() {
        status = ShortUrlStatus.DISABLED;
    }
}