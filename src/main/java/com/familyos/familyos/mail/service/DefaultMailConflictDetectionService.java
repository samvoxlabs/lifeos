package com.familyos.familyos.mail.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.config.properties.GoogleProperties;
import com.familyos.familyos.integrations.google.calendar.GoogleCalendarClient;
import com.familyos.familyos.integrations.google.calendar.GoogleCalendarEvent;
import com.familyos.familyos.mail.entity.MailExtractedEvent;
import com.familyos.familyos.mail.model.DetectedConflict;
import com.familyos.familyos.mail.model.ResolutionOption;
import com.familyos.familyos.mail.repository.MailExtractedEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class DefaultMailConflictDetectionService implements MailConflictDetectionService {

    private static final Set<String> ACTIVE_FAMILY_EVENT_STATUSES = Set.of(
            "PENDING",
            "READY",
            "SCHEDULED",
            "CONFLICTED",
            "proposed",
            "confirmed",
            "conflict",
            "ready_to_publish"
    );

    private final MailExtractedEventRepository mailExtractedEventRepository;
    private final GoogleCalendarClient googleCalendarClient;
    private final GoogleProperties googleProperties;

    public DefaultMailConflictDetectionService(
            MailExtractedEventRepository mailExtractedEventRepository,
            GoogleCalendarClient googleCalendarClient,
            GoogleProperties googleProperties
    ) {
        this.mailExtractedEventRepository = mailExtractedEventRepository;
        this.googleCalendarClient = googleCalendarClient;
        this.googleProperties = googleProperties;
    }

    @Override
    public List<DetectedConflict> detectConflicts(OAuthAccount account, MailExtractedEvent event, String accessToken) {
        List<DetectedConflict> conflicts = new ArrayList<>();

        UUID eventId = event.getId() == null ? new UUID(0L, 0L) : event.getId();
        List<MailExtractedEvent> familyEvents = mailExtractedEventRepository
                .findByMessageAccountAndIdNotAndStatusIn(account, eventId, ACTIVE_FAMILY_EVENT_STATUSES);
        for (MailExtractedEvent candidate : familyEvents) {
            if (overlaps(event.getStartsAt(), event.getEndsAt(), candidate.getStartsAt(), candidate.getEndsAt())) {
                conflicts.add(new DetectedConflict(
                        candidate.getId().toString(),
                        "FAMILY_CALENDAR",
                        max(event.getStartsAt(), candidate.getStartsAt()),
                        min(event.getEndsAt(), candidate.getEndsAt()),
                        defaultResolutionOptions()
                ));
            }
        }

        int maxResults = googleProperties.calendar() != null && googleProperties.calendar().maxResults() != null
                ? googleProperties.calendar().maxResults()
                : 25;
        List<GoogleCalendarEvent> googleEvents = googleCalendarClient.fetchEvents(accessToken, maxResults);
        for (GoogleCalendarEvent calendarEvent : googleEvents) {
            OffsetDateTime startsAt = parseCalendarDate(calendarEvent.start());
            OffsetDateTime endsAt = parseCalendarDate(calendarEvent.end());
            if (startsAt == null || endsAt == null) {
                continue;
            }
            if (overlaps(event.getStartsAt(), event.getEndsAt(), startsAt, endsAt)) {
                conflicts.add(new DetectedConflict(
                        calendarEvent.id(),
                        "GOOGLE_CALENDAR",
                        max(event.getStartsAt(), startsAt),
                        min(event.getEndsAt(), endsAt),
                        defaultResolutionOptions()
                ));
            }
        }

        return conflicts;
    }

    private List<ResolutionOption> defaultResolutionOptions() {
        return List.of(
                new ResolutionOption(
                        "delegate_pickup",
                        "Ask another family member to handle pickup",
                        "Delegate this pickup to another family member without moving the event time.",
                        true,
                        false
                ),
                new ResolutionOption(
                        "delay_pickup_one_hour_notify",
                        "Delay pickup by one hour and notify the arriving family member",
                        "Shift the pickup event by one hour and send a notification.",
                        false,
                        true
                )
        );
    }

    private boolean overlaps(OffsetDateTime aStart, OffsetDateTime aEnd, OffsetDateTime bStart, OffsetDateTime bEnd) {
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }

    private OffsetDateTime max(OffsetDateTime a, OffsetDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private OffsetDateTime min(OffsetDateTime a, OffsetDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    private OffsetDateTime parseCalendarDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (Exception ignored) {
            try {
                return LocalDate.parse(value).atStartOfDay().atOffset(ZoneOffset.UTC);
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }
}
