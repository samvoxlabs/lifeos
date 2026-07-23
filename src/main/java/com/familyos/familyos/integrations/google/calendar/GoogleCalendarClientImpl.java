package com.familyos.familyos.integrations.google.calendar;

import com.familyos.familyos.config.properties.GoogleProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GoogleCalendarClientImpl implements GoogleCalendarClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarClientImpl.class);

    private final RestClient restClient;
    private final GoogleProperties googleProperties;

    public GoogleCalendarClientImpl(GoogleProperties googleProperties) {
        this.googleProperties = googleProperties;
        this.restClient = RestClient.builder()
                .baseUrl(googleProperties.apis().calendarBaseUrl())
                .build();
    }

    @Override
    public List<GoogleCalendarEvent> fetchEvents(String accessToken, int maxResults) {
        log.debug("Fetching {} calendar events from Google Calendar API", maxResults);

        Map<String, Object> response = restClient.get()
                .uri("/calendars/{calendarId}/events?maxResults={maxResults}&singleEvents=true&orderBy=startTime",
                        googleProperties.calendar().calendarId(), maxResults)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getOrDefault("items", List.of());
        log.debug("Retrieved {} calendar events", items.size());

        return items.stream()
                .map(this::toEvent)
                .toList();
    }

    @Override
    public String createCalendarEvent(String accessToken, String calendarId, MailEventForCalendar event) {
        log.info("Creating calendar event: {}", event.title());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("summary", event.title());
        if (event.location() != null) body.put("location", event.location());
        if (event.description() != null) body.put("description", event.description());

        Map<String, String> start = new LinkedHashMap<>();
        start.put("dateTime", event.start());
        body.put("start", start);

        Map<String, String> end = new LinkedHashMap<>();
        end.put("dateTime", event.end());
        body.put("end", end);
        log.info("Calendar payload: {}", body);

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/calendars/{calendarId}/events", calendarId)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            String eventId = response != null ? (String) response.get("id") : null;
            log.info("Created Google Calendar event: {}", eventId);
            return eventId;
        } catch (Exception e) {
            log.error("Failed to create calendar event: {}", e.getMessage());
            throw new RuntimeException("CALENDAR_UPDATE_FAILED: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteCalendarEvent(String accessToken, String calendarId, String eventId) {
        log.info("Deleting calendar event: {}", eventId);
        try {
            restClient.delete()
                    .uri("/calendars/{calendarId}/events/{eventId}", calendarId, eventId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getRawStatusCode() == 404) {
                log.warn("Calendar event already missing: {}", eventId);
                return;
            }
            log.error("Failed to delete calendar event: {}", ex.getMessage());
            throw new RuntimeException("CALENDAR_DELETE_FAILED: " + ex.getMessage(), ex);
        } catch (Exception e) {
            log.error("Failed to delete calendar event: {}", e.getMessage());
            throw new RuntimeException("CALENDAR_DELETE_FAILED: " + e.getMessage(), e);
        }
    }

    private GoogleCalendarEvent toEvent(Map<String, Object> item) {
        return new GoogleCalendarEvent(
                (String) item.get("id"),
                (String) item.getOrDefault("summary", ""),
                (String) item.getOrDefault("location", ""),
                (String) item.getOrDefault("description", ""),
                (String) item.getOrDefault("status", ""),
                extractDateTime(item.get("start")),
                extractDateTime(item.get("end"))
        );
    }

    @SuppressWarnings("unchecked")
    private String extractDateTime(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return "";
        }

        Object dateTime = map.get("dateTime");
        if (dateTime instanceof String s && !s.isBlank()) {
            return s;
        }

        Object date = map.get("date");
        if (date instanceof String s && !s.isBlank()) {
            return s;
        }

        return "";
    }
}
