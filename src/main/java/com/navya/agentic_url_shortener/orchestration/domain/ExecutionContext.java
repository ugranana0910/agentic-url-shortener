package com.navya.agentic_url_shortener.orchestration.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ExecutionContext {

    private final Map<String, Object> values =
            Collections.synchronizedMap(
                    new LinkedHashMap<>()
            );

    public void put(
            String key,
            Object value
    ) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "Context key must not be blank"
            );
        }

        if (value == null) {
            throw new IllegalArgumentException(
                    "Context value must not be null"
            );
        }

        values.put(key, value);
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(values.get(key));
    }

    public <T> Optional<T> get(
            String key,
            Class<T> type
    ) {
        Object value = values.get(key);

        if (value == null) {
            return Optional.empty();
        }

        if (!type.isInstance(value)) {
            throw new IllegalStateException(
                    "Context value for key '"
                            + key
                            + "' is not a "
                            + type.getSimpleName()
            );
        }

        return Optional.of(type.cast(value));
    }

    public boolean contains(String key) {
        return values.containsKey(key);
    }

    public Map<String, Object> snapshot() {
        synchronized (values) {
            return Collections.unmodifiableMap(
                    new LinkedHashMap<>(values)
            );
        }
    }
}