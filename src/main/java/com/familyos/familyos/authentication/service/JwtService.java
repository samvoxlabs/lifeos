package com.familyos.familyos.authentication.service;

import com.familyos.familyos.dto.AuthenticatedUser;
import com.familyos.familyos.config.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
    }

    /**
     * Generates a JWT token for the given authenticated user.
     *
     * @param user the authenticated user
     * @return JWT token string
     */
    public String generateToken(AuthenticatedUser user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.expirationMs());

        return Jwts.builder()
            .subject(user.email())
            .claim("userId", user.id())
            .claim("name", user.name())
            .claim("provider", user.provider())
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact();
    }

    /**
     * Validates the given JWT token.
     *
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Extracts the email (subject) from the given JWT token.
     *
     * @param token the JWT token
     * @return email or empty string if token is invalid
     */
    public String extractEmail(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (JwtException ex) {
            return "";
        }
    }

    /**
     * Extracts the user ID from the given JWT token.
     *
     * @param token the JWT token
     * @return user ID or empty string if token is invalid
     */
    public String extractUserId(String token) {
        try {
            return parseClaims(token).get("userId", String.class);
        } catch (JwtException ex) {
            return "";
        }
    }

    /**
     * Extracts the expiration date from the given JWT token.
     *
     * @param token the JWT token
     * @return expiration date or null if token is invalid
     */
    public Date extractExpiration(String token) {
        try {
            return parseClaims(token).getExpiration();
        } catch (JwtException ex) {
            return null;
        }
    }

    /**
     * Checks if the token has expired.
     *
     * @param token the JWT token
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            parseClaims(token);
            return false;
        } catch (ExpiredJwtException ex) {
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public Optional<AuthenticatedUser> extractAuthenticatedUser(String token) {
        try {
            Claims claims = parseClaims(token);
            String userId = claims.get("userId", String.class);
            String email = claims.getSubject();
            String name = claims.get("name", String.class);
            String provider = claims.get("provider", String.class);

            if (userId == null || userId.isBlank()
                    || email == null || email.isBlank()
                    || name == null || name.isBlank()
                    || provider == null || provider.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new AuthenticatedUser(userId, email, name, provider));
        } catch (JwtException ex) {
            return Optional.empty();
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
