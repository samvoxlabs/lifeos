package com.familyos.familyos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String secret,
    Long expirationMs
) {
    
    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret must not be blank");
        }
        if (expirationMs == null || expirationMs <= 0) {
            throw new IllegalArgumentException("jwt.expiration-ms must be greater than 0");
        }
    }
}
