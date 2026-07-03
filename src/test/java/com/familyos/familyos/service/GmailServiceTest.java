package com.familyos.familyos.service;

import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.repository.OAuthTokenRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GmailServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private OAuthTokenRepository oauthTokenRepository;

    @Mock
    private GoogleGmailClient googleGmailClient;

    private GmailService gmailService;

    @BeforeEach
    void setUp() {
        gmailService = new GmailService(userService, oauthTokenRepository, googleGmailClient);
    }

    @Test
    void readLatestMessagesMapsGoogleMessagesToDtos() {
        User user = user("user@example.com");
        OAuthToken token = new OAuthToken(user, "google", "access-token", "refresh-token", "Bearer", LocalDateTime.now().plusHours(1));

        when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(oauthTokenRepository.findByUserAndProvider(user, "google")).thenReturn(Optional.of(token));
        when(googleGmailClient.fetchMessages("access-token", 10)).thenReturn(List.of(
                new GoogleGmailMessage("1", "thread-1", "sender@example.com", "Subject", "Mon, 1 Jan 2024", "Snippet")
        ));

        List<GmailMessageDto> result = gmailService.readLatestMessages("user@example.com");

        assertEquals(1, result.size());
        assertEquals("1", result.get(0).id());
        assertEquals("sender@example.com", result.get(0).from());
        verify(googleGmailClient).fetchMessages("access-token", 10);
    }

    @Test
    void readLatestMessagesThrowsWhenUserMissing() {
        when(userService.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> gmailService.readLatestMessages("missing@example.com"));
        verifyNoInteractions(oauthTokenRepository, googleGmailClient);
    }

    @Test
    void readLatestMessagesThrowsWhenTokenMissing() {
        User user = user("user@example.com");
        when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(oauthTokenRepository.findByUserAndProvider(user, "google")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> gmailService.readLatestMessages("user@example.com"));
        verifyNoInteractions(googleGmailClient);
    }

    private User user(String email) {
        User user = new User(email, "Test User", "google");
        user.setId(UUID.randomUUID());
        return user;
    }
}
