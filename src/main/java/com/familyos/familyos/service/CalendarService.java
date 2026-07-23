package com.familyos.familyos.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.exception.UnauthorizedException;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.OAuthTokenService;
import com.familyos.familyos.authentication.service.TokenRefreshService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.calendar.dto.CalendarPublishResponse;
import com.familyos.familyos.calendar.dto.CalendarResetResponse;
import com.familyos.familyos.config.properties.GoogleProperties;
import com.familyos.familyos.dto.CalendarEventDto;
import com.familyos.familyos.integrations.google.calendar.GoogleCalendarClient;
import com.familyos.familyos.integrations.google.calendar.GoogleCalendarEvent;
import com.familyos.familyos.integrations.google.calendar.MailEventForCalendar;
import com.familyos.familyos.mail.entity.MailExtractedEvent;
import com.familyos.familyos.mail.repository.MailExtractedEventRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CalendarService {

    private static final Logger log = LoggerFactory.getLogger(CalendarService.class);
    private static final String GOOGLE_PROVIDER = "google";
    private static final int DEFAULT_MAX_RESULTS = 10;
    private static final DateTimeFormatter GOOGLE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    private final UserService userService;
    private final OAuthAccountService oauthAccountService;
    private final OAuthTokenService oauthTokenService;
    private final TokenRefreshService tokenRefreshService;
    private final GoogleCalendarClient googleCalendarClient;
    private final GoogleProperties googleProperties;
    private final MailExtractedEventRepository mailExtractedEventRepository;

    public CalendarService(UserService userService,
                           OAuthAccountService oauthAccountService,
                           OAuthTokenService oauthTokenService,
                           TokenRefreshService tokenRefreshService,
                           GoogleCalendarClient googleCalendarClient,
                           GoogleProperties googleProperties,
                           MailExtractedEventRepository mailExtractedEventRepository) {
        this.userService = userService;
        this.oauthAccountService = oauthAccountService;
        this.oauthTokenService = oauthTokenService;
        this.tokenRefreshService = tokenRefreshService;
        this.googleCalendarClient = googleCalendarClient;
        this.googleProperties = googleProperties;
        this.mailExtractedEventRepository = mailExtractedEventRepository;
    }

    public List<CalendarEventDto> readUpcomingEvents(String userId) {
        log.debug("Reading upcoming calendar events for user: {}", userId);

        User user = resolveUser(userId);

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

    @Transactional
    public CalendarPublishResponse publishEvents(String userId) {
        log.info("Publishing ready_to_publish events to Google Calendar for user {}", userId);

        User user = resolveUser(userId);
        OAuthAccount oauthAccount = oauthAccountService.findByUserAndProvider(user, GOOGLE_PROVIDER)
                .orElseThrow(() -> new UnauthorizedException("Google account not connected"));
        OAuthToken oauthToken = oauthTokenService.findByAccount(oauthAccount)
                .orElseThrow(() -> new UnauthorizedException("Google token not available"));
        String accessToken = tokenRefreshService.getValidAccessToken(oauthToken);

        List<MailExtractedEvent> readyEvents = mailExtractedEventRepository
                .findByMessageAccountAndStatus(oauthAccount, "ready_to_publish");

        List<String> publishedIds = new ArrayList<>();
        for (MailExtractedEvent event : readyEvents) {
            try {
                MailEventForCalendar calEvent = new MailEventForCalendar(
                        event.getTitle(),
                        toCalendarDateTime(event.getStartsAt(), event.getTimezone()),
                        toCalendarDateTime(event.getEndsAt(), event.getTimezone()),
                        event.getTimezone() != null ? event.getTimezone() : "America/Chicago",
                        event.getLocation(),
                        event.getEventDescription()
                );
                String gcalId = googleCalendarClient.createCalendarEvent(
                        accessToken,
                        googleProperties.calendar().calendarId(),
                        calEvent
                );
                event.setGoogleCalendarEventId(gcalId);
                event.setStatus("published");
                mailExtractedEventRepository.save(event);
                publishedIds.add("event-" + event.getId());
                log.info("Published event {} to Google Calendar with ID {}", event.getId(), gcalId);
            } catch (Exception e) {
                log.warn("Failed to publish event {}: {}", event.getId(), e.getMessage());
            }
        }

        return new CalendarPublishResponse(
                publishedIds.isEmpty() ? "NO_EVENTS" : "PUBLISHED",
                publishedIds.size(),
                publishedIds,
                OffsetDateTime.now().toString()
        );
    }

    @Transactional
    public CalendarResetResponse resetPublishedEvents(String userId) {
        log.info("Resetting published calendar events for user {}", userId);

        User user = resolveUser(userId);
        OAuthAccount oauthAccount = oauthAccountService.findByUserAndProvider(user, GOOGLE_PROVIDER)
                .orElseThrow(() -> new UnauthorizedException("Google account not connected"));
        OAuthToken oauthToken = oauthTokenService.findByAccount(oauthAccount)
                .orElseThrow(() -> new UnauthorizedException("Google token not available"));
        String accessToken = tokenRefreshService.getValidAccessToken(oauthToken);

        List<MailExtractedEvent> demoEvents = mailExtractedEventRepository
                .findByMessageAccountOrderByStartsAtDesc(oauthAccount, org.springframework.data.domain.PageRequest.of(0, 250));
        List<GoogleCalendarEvent> calendarEvents = googleCalendarClient.fetchEvents(accessToken, 250);

        List<String> resetEventIds = new ArrayList<>();
        for (MailExtractedEvent event : demoEvents) {
            String gcalEventId = event.getGoogleCalendarEventId();
            if (gcalEventId == null || gcalEventId.isBlank()) {
                gcalEventId = findMatchingCalendarEventId(event, calendarEvents);
            }

            if (gcalEventId != null && !gcalEventId.isBlank()) {
                googleCalendarClient.deleteCalendarEvent(accessToken, googleProperties.calendar().calendarId(), gcalEventId);
                resetEventIds.add("event-" + event.getId());
            }

            event.setGoogleCalendarEventId(null);
            event.setStatus("ready_to_publish");
            mailExtractedEventRepository.save(event);
        }

        return new CalendarResetResponse(
                resetEventIds.isEmpty() ? "NO_EVENTS" : "RESET",
                resetEventIds.size(),
                resetEventIds,
                OffsetDateTime.now().toString()
        );
    }

    private User resolveUser(String userId) {
        try {
            return userService.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("User not found");
        }
    }

    private String toCalendarDateTime(OffsetDateTime value, String timezone) {
        if (value == null) {
            return null;
        }

        String zoneId = timezone != null && !timezone.isBlank() ? timezone : "America/Chicago";
        try {
            return value.atZoneSameInstant(ZoneId.of(zoneId)).toOffsetDateTime().format(GOOGLE_DATE_TIME);
        } catch (DateTimeException ex) {
            return value.format(GOOGLE_DATE_TIME);
        }
    }

    private String findMatchingCalendarEventId(MailExtractedEvent event, List<GoogleCalendarEvent> calendarEvents) {
        String expectedStart = toCalendarDateTime(event.getStartsAt(), event.getTimezone());
        String expectedEnd = toCalendarDateTime(event.getEndsAt(), event.getTimezone());
        return calendarEvents.stream()
                .filter(calendarEvent -> event.getTitle().equals(calendarEvent.summary()))
                .filter(calendarEvent -> expectedStart != null && expectedStart.equals(calendarEvent.start()))
                .filter(calendarEvent -> expectedEnd != null && expectedEnd.equals(calendarEvent.end()))
                .map(GoogleCalendarEvent::id)
                .findFirst()
                .orElse(null);
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
