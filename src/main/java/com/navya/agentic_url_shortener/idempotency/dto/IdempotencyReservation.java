package com.navya.agentic_url_shortener.idempotency.dto;

import lombok.Value;

import java.util.UUID;

@Value
public class IdempotencyReservation {

    boolean replay;
    UUID resourceId;
    Integer responseStatus;

    public static IdempotencyReservation acquired() {
        return new IdempotencyReservation(
                false,
                null,
                null
        );
    }

    public static IdempotencyReservation replay(
            UUID resourceId,
            int responseStatus
    ) {
        return new IdempotencyReservation(
                true,
                resourceId,
                responseStatus
        );
    }
}