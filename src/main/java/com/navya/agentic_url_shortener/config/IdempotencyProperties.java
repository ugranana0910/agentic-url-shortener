package com.navya.agentic_url_shortener.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Data
@Validated
@ConfigurationProperties(prefix = "idempotency")
public class IdempotencyProperties {

    @NotNull
    private Duration retention = Duration.ofHours(24);

    @NotNull
    private Duration inProgressTimeout = Duration.ofMinutes(2);

    @Min(16)
    @Max(255)
    private int maxKeyLength = 128;
}