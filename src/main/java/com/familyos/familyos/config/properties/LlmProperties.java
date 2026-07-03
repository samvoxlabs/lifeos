package com.familyos.familyos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LlmProperties(
    String defaultProvider
) {
    
    public LlmProperties {
        if (defaultProvider == null || defaultProvider.isBlank()) {
            throw new IllegalArgumentException("llm.default-provider must not be blank");
        }
    }
}
