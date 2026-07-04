package com.familyos.familyos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm.openrouter")
public record OpenRouterProperties(
    String apiKey,
    String model,
    String baseUrl
) {}
