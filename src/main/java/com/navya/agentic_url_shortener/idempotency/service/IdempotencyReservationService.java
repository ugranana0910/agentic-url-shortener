package com.navya.agentic_url_shortener.idempotency.service;

import com.navya.agentic_url_shortener.config.IdempotencyProperties;
import com.navya.agentic_url_shortener.idempotency.domain.IdempotencyRecord;
import com.navya.agentic_url_shortener.idempotency.dto.IdempotencyReservation;
import com.navya.agentic_url_shortener.idempotency.exception.IdempotencyConflictException;
import com.navya.agentic_url_shortener.idempotency.exception.IdempotencyInProgressException;
import com.navya.agentic_url_shortener.idempotency.repository.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class IdempotencyReservationService {

    private final IdempotencyRecordRepository repository;
    private final IdempotencyProperties properties;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyReservation reserve(
            String key,
            String requestHash
    ) {
        Instant now = clock.instant();

        var existing = repository.findByKeyForUpdate(key);

        if (existing.isPresent()) {
            return evaluateExisting(
                    existing.get(),
                    requestHash,
                    now
            );
        }

        try {
            IdempotencyRecord record =
                    new IdempotencyRecord(
                            key,
                            requestHash,
                            now,
                            now.plus(properties.getRetention())
                    );

            repository.saveAndFlush(record);

            return IdempotencyReservation.acquired();
        } catch (DataIntegrityViolationException race) {
            /*
             * Another transaction reserved the same primary key.
             * This transaction must roll back; the coordinator retries
             * reservation in a fresh transaction.
             */
            throw race;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyReservation inspectAfterRace(
            String key,
            String requestHash
    ) {
        IdempotencyRecord record =
                repository.findByKeyForUpdate(key)
                        .orElseThrow(
                                IdempotencyInProgressException::new
                        );

        return evaluateExisting(
                record,
                requestHash,
                clock.instant()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String key) {
        repository.findByKeyForUpdate(key)
                .ifPresent(record ->
                        record.fail(clock.instant())
                );
    }

    private IdempotencyReservation evaluateExisting(
            IdempotencyRecord record,
            String requestHash,
            Instant now
    ) {
        if (!record.hasRequestHash(requestHash)) {
            throw new IdempotencyConflictException();
        }

        if (record.isCompleted()) {
            return IdempotencyReservation.replay(
                    record.getResourceId(),
                    record.getResponseStatus()
            );
        }

        if (record.isFailed() ||
                record.isExpiredAt(now) ||
                record.hasTimedOutAt(
                        now,
                        properties.getInProgressTimeout()
                )) {
            record.restart(
                    now,
                    now.plus(properties.getRetention())
            );

            return IdempotencyReservation.acquired();
        }

        throw new IdempotencyInProgressException();
    }
}