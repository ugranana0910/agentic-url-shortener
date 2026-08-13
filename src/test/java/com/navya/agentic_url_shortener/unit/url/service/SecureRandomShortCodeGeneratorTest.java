package com.navya.agentic_url_shortener.unit.url.service;

import com.navya.agentic_url_shortener.url.service.SecureRandomShortCodeGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecureRandomShortCodeGeneratorTest {

    private final SecureRandomShortCodeGenerator generator =
            new SecureRandomShortCodeGenerator();

    @Test
    void generatesRequestedBase62Length() {
        String result = generator.generate(8);

        assertThat(result)
                .hasSize(8)
                .matches("[0-9A-Za-z]{8}");
    }

    @Test
    void rejectsUnsafeCodeLength() {
        assertThatThrownBy(() -> generator.generate(5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Short-code length must be between 6 and 32"
                );
    }
}