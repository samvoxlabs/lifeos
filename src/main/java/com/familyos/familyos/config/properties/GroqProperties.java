package com.familyos.familyos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm.groq")
public record GroqProperties(
    String apiKey,
    String model,
    String baseUrl
) {}
