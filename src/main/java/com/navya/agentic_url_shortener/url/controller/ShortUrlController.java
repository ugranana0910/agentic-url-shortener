package com.navya.agentic_url_shortener.url.controller;

import com.navya.agentic_url_shortener.url.dto.CreateShortUrlRequest;
import com.navya.agentic_url_shortener.url.dto.RedirectTarget;
import com.navya.agentic_url_shortener.url.dto.ShortUrlResponse;
import com.navya.agentic_url_shortener.url.service.ShortUrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    @PostMapping("/api/v1/urls")
    public ResponseEntity<ShortUrlResponse> create(
            @Valid @RequestBody CreateShortUrlRequest request
    ) {
        ShortUrlResponse response =
                shortUrlService.create(request);

        URI resourceLocation = URI.create(
                "/api/v1/urls/" + response.getShortCode()
        );

        return ResponseEntity
                .created(resourceLocation)
                .body(response);
    }

    @GetMapping("/api/v1/urls/{shortCode}")
    public ShortUrlResponse get(
            @PathVariable String shortCode
    ) {
        return shortUrlService.get(shortCode);
    }

    @GetMapping("/{shortCode:[0-9A-Za-z]{6,32}}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode
    ) {
        RedirectTarget target =
                shortUrlService.resolve(shortCode);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(
                        HttpHeaders.LOCATION,
                        target.getOriginalUrl()
                )
                .build();
    }
}