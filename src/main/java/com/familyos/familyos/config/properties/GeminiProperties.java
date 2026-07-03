package com.familyos.familyos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm.gemini")
public record GeminiProperties(
    String apiKey,
    String model,
    String baseUrl
) {}
