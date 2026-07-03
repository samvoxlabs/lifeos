package com.familyos.familyos.authentication.jwt;

import com.familyos.familyos.config.properties.JwtProperties;
import com.familyos.familyos.authentication.service.JwtService;
import com.familyos.familyos.dto.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtProperties jwtProperties;
    private AuthenticatedUser testUser;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties("test-secret-key-that-is-long-enough-for-hs256-algorithm", 3600000L);
        jwtService = new JwtService(jwtProperties);
        testUser = new AuthenticatedUser(
            "user123",
            "test@example.com",
            "Test User",
            "google"
        );
    }

    @Test
    void testGenerateToken_Success() {
        String token = jwtService.generateToken(testUser);
        
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(token.contains("."));
    }

    @Test
    void testValidateToken_Valid() {
        String token = jwtService.generateToken(testUser);
        
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testValidateToken_Invalid() {
        String invalidToken = "invalid.token.here";
        
        assertFalse(jwtService.validateToken(invalidToken));
    }

    @Test
    void testValidateToken_Expired() {
        JwtProperties expiredProperties = new JwtProperties(
            "test-secret-key-that-is-long-enough-for-hs256-algorithm",
            100L // 100ms expiration
        );
        JwtService expiredService = new JwtService(expiredProperties);
        
        String token = expiredService.generateToken(testUser);
        
        // Wait for token to expire
        try {
            Thread.sleep(150); // Wait longer than expiration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertFalse(expiredService.validateToken(token));
    }

    @Test
    void testExtractEmail_Success() {
        String token = jwtService.generateToken(testUser);
        String email = jwtService.extractEmail(token);
        
        assertEquals(testUser.email(), email);
    }

    @Test
    void testExtractEmail_InvalidToken() {
        String email = jwtService.extractEmail("invalid.token.here");
        
        assertEquals("", email);
    }

    @Test
    void testExtractUserId_Success() {
        String token = jwtService.generateToken(testUser);
        String userId = jwtService.extractUserId(token);
        
        assertEquals(testUser.id(), userId);
    }

    @Test
    void testExtractUserId_InvalidToken() {
        String userId = jwtService.extractUserId("invalid.token.here");
        
        assertEquals("", userId);
    }

    @Test
    void testExtractExpiration_Success() {
        String token = jwtService.generateToken(testUser);
        Date expiration = jwtService.extractExpiration(token);
        
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testExtractExpiration_InvalidToken() {
        Date expiration = jwtService.extractExpiration("invalid.token.here");
        
        assertNull(expiration);
    }

    @Test
    void testIsTokenExpired_NotExpired() {
        String token = jwtService.generateToken(testUser);
        
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void testIsTokenExpired_Expired() {
        JwtProperties expiredProperties = new JwtProperties(
            "test-secret-key-that-is-long-enough-for-hs256-algorithm",
            100L // 100ms expiration
        );
        JwtService expiredService = new JwtService(expiredProperties);
        
        String token = expiredService.generateToken(testUser);
        
        try {
            Thread.sleep(150); // Wait longer than expiration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertTrue(expiredService.isTokenExpired(token));
    }

    @Test
    void testTokenContainsAllClaims() {
        String token = jwtService.generateToken(testUser);
        
        assertEquals(testUser.email(), jwtService.extractEmail(token));
        assertEquals(testUser.id(), jwtService.extractUserId(token));
        assertNotNull(jwtService.extractExpiration(token));
    }

    @Test
    void testJwtPropertiesValidation_NullSecret() {
        assertThrows(IllegalArgumentException.class, () -> 
            new JwtProperties(null, 3600000L)
        );
    }

    @Test
    void testJwtPropertiesValidation_BlankSecret() {
        assertThrows(IllegalArgumentException.class, () -> 
            new JwtProperties("", 3600000L)
        );
    }

    @Test
    void testJwtPropertiesValidation_NullExpiration() {
        assertThrows(IllegalArgumentException.class, () -> 
            new JwtProperties("test-secret-key-that-is-long-enough-for-hs256-algorithm", null)
        );
    }

    @Test
    void testJwtPropertiesValidation_ZeroExpiration() {
        assertThrows(IllegalArgumentException.class, () -> 
            new JwtProperties("test-secret-key-that-is-long-enough-for-hs256-algorithm", 0L)
        );
    }

    @Test
    void testDifferentUsersGenerateDifferentTokens() {
        AuthenticatedUser user2 = new AuthenticatedUser(
            "user456",
            "test2@example.com",
            "Another User",
            "github"
        );
        
        String token1 = jwtService.generateToken(testUser);
        String token2 = jwtService.generateToken(user2);
        
        assertNotEquals(token1, token2);
        assertEquals(testUser.email(), jwtService.extractEmail(token1));
        assertEquals(user2.email(), jwtService.extractEmail(token2));
    }

    @Test
    void testTokenSignatureVerification() {
        String token = jwtService.generateToken(testUser);
        
        // Create a new service with different secret - should fail validation
        JwtProperties differentProperties = new JwtProperties(
            "different-secret-key-that-is-long-enough-for-hs256",
            3600000L
        );
        JwtService differentService = new JwtService(differentProperties);
        
        assertFalse(differentService.validateToken(token));
    }
}
