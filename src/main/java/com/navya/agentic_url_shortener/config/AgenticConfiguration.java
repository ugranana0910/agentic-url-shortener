package com.navya.agentic_url_shortener.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AgenticProperties.class)
public class AgenticConfiguration {
}
