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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarService {

    private static final Logger log = LoggerFactory.getLogger(CalendarService.class);
    private static final String GOOGLE_PROVIDER = "google";
    private static final int DEFAULT_MAX_RESULTS = 10;

    private final UserService userService;
    private final OAuthAccountService oauthAccountService;
    private final OAuthTokenService oauthTokenService;
    private final TokenRefreshService tokenRefreshService;
    private final GoogleCalendarClient googleCalendarClient;
    private final GoogleProperties googleProperties;

    public CalendarService(UserService userService,
                           OAuthAccountService oauthAccountService,
                           OAuthTokenService oauthTokenService,
                           TokenRefreshService tokenRefreshService,
                           GoogleCalendarClient googleCalendarClient,
                           GoogleProperties googleProperties) {
        this.userService = userService;
        this.oauthAccountService = oauthAccountService;
        this.oauthTokenService = oauthTokenService;
        this.tokenRefreshService = tokenRefreshService;
        this.googleCalendarClient = googleCalendarClient;
        this.googleProperties = googleProperties;
    }

    public List<CalendarEventDto> readUpcomingEvents(String userId) {
        log.debug("Reading upcoming calendar events for user: {}", userId);

        User user;
        try {
            user = userService.findById(java.util.UUID.fromString(userId))
                    .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Authenticated user not found");
        }

        OAuthAccount oauthAccount = oauthAccountService.findByUserAndProvider(user, GOOGLE_PROVIDER)
                .orElseThrow(() -> new UnauthorizedException("Google account not connected"));

        OAuthToken oauthToken = oauthTokenService.findByAccount(oauthAccount)
                .orElseThrow(() -> new UnauthorizedException("Google token not available"));
        String accessToken = tokenRefreshService.getValidAccessToken(oauthToken);

        int maxResults = googleProperties.calendar() != null && googleProperties.calendar().maxResults() != null
                ? googleProperties.calendar().maxResults()
                : DEFAULT_MAX_RESULTS;

        log.debug("Calling Google Calendar API for user: {}", user.getEmail());
        List<GoogleCalendarEvent> calendarEvents = googleCalendarClient.fetchEvents(accessToken, maxResults);

        log.debug("Converting {} calendar events to DTOs", calendarEvents.size());
        return calendarEvents.stream()
                .map(this::convertToDto)
                .toList();
    }

    private CalendarEventDto convertToDto(GoogleCalendarEvent event) {
        return new CalendarEventDto(
                event.id(),
                event.summary(),
                event.location(),
                event.description(),
                event.status(),
                event.start(),
                event.end()
        );
    }
}
