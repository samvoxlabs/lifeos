package com.familyos.familyos.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.exception.UnauthorizedException;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.OAuthTokenService;
import com.familyos.familyos.authentication.service.TokenRefreshService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.config.properties.GoogleProperties;
import com.familyos.familyos.dto.CalendarEventDto;
import com.familyos.familyos.integrations.google.calendar.GoogleCalendarClient;
import com.familyos.familyos.integrations.google.calendar.GoogleCalendarEvent;
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
class CalendarServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private OAuthAccountService oauthAccountService;

    @Mock
    private OAuthTokenService oauthTokenService;

    @Mock
    private TokenRefreshService tokenRefreshService;

    @Mock
    private GoogleCalendarClient googleCalendarClient;

    private CalendarService calendarService;

    @BeforeEach
    void setUp() {
        GoogleProperties properties = new GoogleProperties(
                new GoogleProperties.Apis(
                        "https://gmail.googleapis.com/gmail/v1",
                        "https://www.googleapis.com/calendar/v3",
                        "https://www.googleapis.com/drive/v3",
                        "https://tasks.googleapis.com/tasks/v1",
                        "https://people.googleapis.com/v1"
                ),
                new GoogleProperties.Gmail("me", 10),
                new GoogleProperties.Calendar("primary", 10),
                new GoogleProperties.Drive(10),
                new GoogleProperties.Tasks(10),
                new GoogleProperties.People(20)
        );
        calendarService = new CalendarService(userService, oauthAccountService, oauthTokenService, tokenRefreshService, googleCalendarClient, properties);
    }

    @Test
    void readUpcomingEventsMapsGoogleEventsToDtos() {
        User user = user("user@example.com");
        OAuthAccount account = new OAuthAccount(user, "google", "subject-1", "user@example.com", "Test User");
        OAuthToken token = new OAuthToken(account, "access-token", "refresh-token", "Bearer", "openid email", LocalDateTime.now().plusHours(1));

        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(oauthAccountService.findByUserAndProvider(user, "google")).thenReturn(Optional.of(account));
        when(oauthTokenService.findByAccount(account)).thenReturn(Optional.of(token));
        when(tokenRefreshService.getValidAccessToken(token)).thenReturn("access-token");
        when(googleCalendarClient.fetchEvents("access-token", 10)).thenReturn(List.of(
                new GoogleCalendarEvent("1", "Standup", "Zoom", "Daily standup", "confirmed", "2026-07-03T09:00:00-05:00", "2026-07-03T09:15:00-05:00")
        ));

        List<CalendarEventDto> result = calendarService.readUpcomingEvents(user.getId().toString());

        assertEquals(1, result.size());
        assertEquals("1", result.get(0).id());
        assertEquals("Standup", result.get(0).summary());
        verify(googleCalendarClient).fetchEvents("access-token", 10);
    }

    @Test
    void readUpcomingEventsThrowsWhenUserMissing() {
        UUID userId = UUID.randomUUID();
        when(userService.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> calendarService.readUpcomingEvents(userId.toString()));
        verifyNoInteractions(oauthAccountService, oauthTokenService, tokenRefreshService, googleCalendarClient);
    }

    @Test
    void readUpcomingEventsThrowsWhenTokenMissing() {
        User user = user("user@example.com");
        OAuthAccount account = new OAuthAccount(user, "google", "subject-1", "user@example.com", "Test User");
        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(oauthAccountService.findByUserAndProvider(user, "google")).thenReturn(Optional.of(account));
        when(oauthTokenService.findByAccount(account)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> calendarService.readUpcomingEvents(user.getId().toString()));
        verifyNoInteractions(tokenRefreshService, googleCalendarClient);
    }

    private User user(String email) {
        User user = new User(email, "Test User", "google");
        user.setId(UUID.randomUUID());
        return user;
    }
}
