package com.navya.agentic_url_shortener.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "url-shortener")
public class UrlShortenerProperties {

    @NotBlank
    private String baseUrl;

    @Min(6)
    @Max(32)
    private int codeLength = 8;

    @Min(1)
    @Max(100)
    private int generationAttempts = 10;
}
