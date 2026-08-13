package com.navya.agentic_url_shortener.idempotency.dto;

import lombok.Value;

@Value
public class IdempotentResult<T> {

    T body;
    int responseStatus;
    boolean replayed;

    public static <T> IdempotentResult<T> created(
            T body,
            int responseStatus
    ) {
        return new IdempotentResult<>(
                body,
                responseStatus,
                false
        );
    }

    public static <T> IdempotentResult<T> replayed(
            T body,
            int responseStatus
    ) {
        return new IdempotentResult<>(
                body,
                responseStatus,
                true
        );
    }
}