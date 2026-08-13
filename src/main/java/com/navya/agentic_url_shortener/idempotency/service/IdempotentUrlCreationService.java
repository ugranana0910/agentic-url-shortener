package com.navya.agentic_url_shortener.idempotency.service;

import com.navya.agentic_url_shortener.config.IdempotencyProperties;
import com.navya.agentic_url_shortener.idempotency.domain.IdempotencyRecord;
import com.navya.agentic_url_shortener.idempotency.dto.IdempotentResult;
import com.navya.agentic_url_shortener.idempotency.dto.IdempotencyReservation;
import com.navya.agentic_url_shortener.idempotency.exception.InvalidIdempotencyKeyException;
import com.navya.agentic_url_shortener.idempotency.repository.IdempotencyRecordRepository;
import com.navya.agentic_url_shortener.url.dto.CreateShortUrlRequest;
import com.navya.agentic_url_shortener.url.dto.ShortUrlResponse;
import com.navya.agentic_url_shortener.url.service.ShortUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class IdempotentUrlCreationService {

    private final IdempotencyReservationService reservationService;
    private final IdempotencyRecordRepository repository;
    private final RequestFingerprintService fingerprintService;
    private final ShortUrlService shortUrlService;
    private final IdempotencyProperties properties;
    private final TransactionTemplate transactionTemplate;
    private final java.time.Clock clock;

    public IdempotentResult<ShortUrlResponse> create(
            String idempotencyKey,
            CreateShortUrlRequest request
    ) {
        String normalizedKey =
                validateAndNormalizeKey(idempotencyKey);

        String requestHash =
                fingerprintService.fingerprint(request);

        IdempotencyReservation reservation;

        try {
            reservation = reservationService.reserve(
                    normalizedKey,
                    requestHash
            );
        } catch (DataIntegrityViolationException race) {
            reservation = reservationService.inspectAfterRace(
                    normalizedKey,
                    requestHash
            );
        }

        if (reservation.isReplay()) {
            ShortUrlResponse response =
                    shortUrlService.getById(
                            reservation.getResourceId()
                    );

            return IdempotentResult.replayed(
                    response,
                    reservation.getResponseStatus()
            );
        }

        try {
            IdempotentResult<ShortUrlResponse> result =
                    transactionTemplate.execute(status -> {
                        IdempotencyRecord record =
                                repository.findByKeyForUpdate(
                                                normalizedKey
                                        )
                                        .orElseThrow(
                                                () ->
                                                        new IllegalStateException(
                                                                "Idempotency reservation disappeared"
                                                        )
                                        );

                        ShortUrlResponse response =
                                shortUrlService.create(request);

                        record.complete(
                                response.getId(),
                                HttpStatus.CREATED.value(),
                                clock.instant()
                        );

                        return IdempotentResult.created(
                                response,
                                HttpStatus.CREATED.value()
                        );
                    });

            if (result == null) {
                throw new IllegalStateException(
                        "Idempotent transaction returned no result"
                );
            }

            return result;
        } catch (RuntimeException exception) {
            reservationService.markFailed(normalizedKey);
            throw exception;
        }
    }

    private String validateAndNormalizeKey(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            throw new InvalidIdempotencyKeyException(
                    "Idempotency-Key header is required"
            );
        }

        String key = candidate.trim();

        if (key.length() > properties.getMaxKeyLength()) {
            throw new InvalidIdempotencyKeyException(
                    "Idempotency-Key must not exceed "
                            + properties.getMaxKeyLength()
                            + " characters"
            );
        }

        if (!key.matches("[A-Za-z0-9._:-]+")) {
            throw new InvalidIdempotencyKeyException(
                    "Idempotency-Key contains unsupported characters"
            );
        }

        return key;
    }
}