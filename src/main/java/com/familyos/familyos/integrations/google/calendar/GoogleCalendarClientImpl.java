package com.familyos.familyos.integrations.google.calendar;

import com.familyos.familyos.config.properties.GoogleProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
