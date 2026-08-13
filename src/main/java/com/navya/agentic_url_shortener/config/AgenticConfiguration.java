package com.navya.agentic_url_shortener.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties({
        AgenticProperties.class,
        UrlShortenerProperties.class
})
public class AgenticConfiguration {

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}