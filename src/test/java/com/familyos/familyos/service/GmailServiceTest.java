package com.familyos.familyos.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.exception.UnauthorizedException;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.OAuthTokenService;
import com.familyos.familyos.authentication.service.TokenRefreshService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.dto.GmailMessageDto;
import com.familyos.familyos.integrations.google.gmail.GoogleGmailClient;
import com.familyos.familyos.integrations.google.gmail.GoogleGmailMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GmailServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private OAuthAccountService oauthAccountService;

    @Mock
    private OAuthTokenService oauthTokenService;

    @Mock
    private TokenRefreshService tokenRefreshService;

    @Mock
    private GoogleGmailClient googleGmailClient;

    private GmailService gmailService;

    @BeforeEach
    void setUp() {
        gmailService = new GmailService(userService, oauthAccountService, oauthTokenService, tokenRefreshService, googleGmailClient);
    }

    @Test
    void readLatestMessagesMapsGoogleMessagesToDtos() {
        User user = user("user@example.com");
        OAuthAccount account = new OAuthAccount(user, "google", "subject-1", "user@example.com", "Test User");
        OAuthToken token = new OAuthToken(account, "access-token", "refresh-token", "Bearer", "openid email", LocalDateTime.now().plusHours(1));

        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(oauthAccountService.findByUserAndProvider(user, "google")).thenReturn(Optional.of(account));
        when(oauthTokenService.findByAccount(account)).thenReturn(Optional.of(token));
        when(tokenRefreshService.getValidAccessToken(token)).thenReturn("access-token");
        when(googleGmailClient.fetchMessages("access-token", 10)).thenReturn(List.of(
                new GoogleGmailMessage("1", "thread-1", "sender@example.com", "Subject", "Mon, 1 Jan 2024", "Snippet")
        ));

        List<GmailMessageDto> result = gmailService.readLatestMessages(user.getId().toString());

        assertEquals(1, result.size());
        assertEquals("1", result.get(0).id());
        assertEquals("sender@example.com", result.get(0).from());
        verify(googleGmailClient).fetchMessages("access-token", 10);
    }

    @Test
    void readLatestMessagesThrowsWhenUserMissing() {
        UUID userId = UUID.randomUUID();
        when(userService.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> gmailService.readLatestMessages(userId.toString()));
        verifyNoInteractions(oauthAccountService, oauthTokenService, tokenRefreshService, googleGmailClient);
    }

    @Test
    void readLatestMessagesThrowsWhenTokenMissing() {
        User user = user("user@example.com");
        OAuthAccount account = new OAuthAccount(user, "google", "subject-1", "user@example.com", "Test User");
        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(oauthAccountService.findByUserAndProvider(user, "google")).thenReturn(Optional.of(account));
        when(oauthTokenService.findByAccount(account)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> gmailService.readLatestMessages(user.getId().toString()));
        verifyNoInteractions(tokenRefreshService, googleGmailClient);
    }

    @Test
    void readLatestMessagesRefreshesExpiredToken() {
        User user = user("user@example.com");
        OAuthAccount account = new OAuthAccount(user, "google", "subject-1", "user@example.com", "Test User");
        OAuthToken token = new OAuthToken(account, "expired-token", "refresh-token", "Bearer", "openid email", LocalDateTime.now().minusHours(1));

        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(oauthAccountService.findByUserAndProvider(user, "google")).thenReturn(Optional.of(account));
        when(oauthTokenService.findByAccount(account)).thenReturn(Optional.of(token));
        when(tokenRefreshService.getValidAccessToken(token)).thenReturn("fresh-token");
        when(googleGmailClient.fetchMessages("fresh-token", 10)).thenReturn(List.of());

        gmailService.readLatestMessages(user.getId().toString());

        verify(tokenRefreshService).getValidAccessToken(token);
        verify(googleGmailClient).fetchMessages("fresh-token", 10);
    }

    private User user(String email) {
        User user = new User(email, "Test User", "google");
        user.setId(UUID.randomUUID());
        return user;
    }
}
