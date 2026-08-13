package com.navya.agentic_url_shortener.url.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateShortUrlRequest {

    @NotBlank(message = "url is required")
    @Size(
            max = 2048,
            message = "url must not exceed 2048 characters"
    )
    private String url;

    @Future(message = "expiresAt must be in the future")
    private Instant expiresAt;
}