package com.familyos.familyos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google")
public record GoogleProperties(
    Apis apis,
    Gmail gmail
) {
    
    public record Apis(
        String gmailBaseUrl
    ) {}
    
    public record Gmail(
        String userId,
        Integer maxResults
    ) {}
}
