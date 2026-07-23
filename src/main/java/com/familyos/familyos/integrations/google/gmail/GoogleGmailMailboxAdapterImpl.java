package com.familyos.familyos.integrations.google.gmail;

import com.familyos.familyos.config.properties.GoogleProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class GoogleGmailMailboxAdapterImpl implements GoogleGmailMailboxAdapter {

    private static final DateTimeFormatter RFC_1123 = DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.ENGLISH);
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GoogleGmailMailboxAdapterImpl.class);


    private final RestClient restClient;
    private final GoogleProperties googleProperties;

    public GoogleGmailMailboxAdapterImpl(GoogleProperties googleProperties) {
        this.googleProperties = googleProperties;
        this.restClient = RestClient.builder()
                .baseUrl(googleProperties.apis().gmailBaseUrl())
                .build();
    }

    @Override
    public GoogleGmailMailboxSyncResult fetchNewMessages(String accessToken, String historyCursor, int maxResults) {
        Set<String> messageIds = historyCursor != null && !historyCursor.isBlank()
                ? fetchMessageIdsFromHistory(accessToken, historyCursor, maxResults)
                : fetchRecentMessageIds(accessToken, maxResults);

        List<GoogleGmailNormalizedMessage> messages = messageIds.stream()
                .map(messageId -> fetchMessageDetails(accessToken, messageId))
                .toList();

        return new GoogleGmailMailboxSyncResult(messages, fetchCurrentHistoryId(accessToken));
    }

    @Override
    public List<GoogleGmailNormalizedMessage> fetchMessages(String accessToken, int maxResults, String query) {
        Set<String> messageIds = fetchMessageIds(accessToken, maxResults, query);
        return messageIds.stream()
                .map(messageId -> fetchMessageDetails(accessToken, messageId))
                .toList();
    }

    @Override
    public String insertMessage(String accessToken, String rawMimeBase64Url, List<String> labelIds) {
        log.info("Inserting demo message into Gmail");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("raw", rawMimeBase64Url);
        if (labelIds != null && !labelIds.isEmpty()) {
            body.put("labelIds", labelIds);
        }
        try {
            Map<String, Object> response = restClient.post()
                    .uri("/users/me/messages/import")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return response != null ? (String) response.get("id") : null;
        } catch (Exception e) {
            log.warn("Failed to insert message into Gmail: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void trashMessage(String accessToken, String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return;
        }
        try {
            restClient.post()
                    .uri("/users/{userId}/messages/{id}/trash", googleProperties.gmail().userId(), messageId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Trashed Gmail message {}", messageId);
        }catch (RestClientResponseException ex) {
          log.error("Status: {}", ex.getStatusCode());
          log.error("Response: {}", ex.getResponseBodyAsString());
          throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> fetchMessageIds(String accessToken, int maxResults, String query) {
        Map<String, Object> response = restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/users/{userId}/messages")
                            .queryParam("maxResults", maxResults);
                    if (query != null && !query.isBlank()) {
                        builder.queryParam("q", query);
                    }
                    return builder.build(googleProperties.gmail().userId());
                })
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> messages =
                response == null ? List.of() : (List<Map<String, Object>>) response.getOrDefault("messages", List.of());

        Set<String> ids = new LinkedHashSet<>();
        for (Map<String, Object> message : messages) {
            Object id = message.get("id");
            if (id instanceof String s && !s.isBlank()) {
                ids.add(s);
            }
        }
        return ids;
    }

    @SuppressWarnings("unchecked")
    private Set<String> fetchMessageIdsFromHistory(String accessToken, String historyCursor, int maxResults) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/users/{userId}/history")
                            .queryParam("startHistoryId", historyCursor)
                            .queryParam("historyTypes", "messageAdded")
                            .queryParam("maxResults", maxResults)
                            .build(googleProperties.gmail().userId()))
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> historyItems =
                    response == null ? List.of() : (List<Map<String, Object>>) response.getOrDefault("history", List.of());
            Set<String> messageIds = new LinkedHashSet<>();

            for (Map<String, Object> historyItem : historyItems) {
                List<Map<String, Object>> messagesAdded =
                        (List<Map<String, Object>>) historyItem.getOrDefault("messagesAdded", List.of());
                for (Map<String, Object> added : messagesAdded) {
                    Map<String, Object> message = (Map<String, Object>) added.get("message");
                    if (message == null) {
                        continue;
                    }
                    Object id = message.get("id");
                    if (id instanceof String s && !s.isBlank()) {
                        messageIds.add(s);
                    }
                }
            }
            return messageIds;
        } catch (HttpClientErrorException.NotFound ex) {
            return fetchRecentMessageIds(accessToken, maxResults);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> fetchRecentMessageIds(String accessToken, int maxResults) {
        Map<String, Object> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/{userId}/messages")
                        .queryParam("maxResults", maxResults)
                        .build(googleProperties.gmail().userId()))
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> messages =
                response == null ? List.of() : (List<Map<String, Object>>) response.getOrDefault("messages", List.of());

        Set<String> ids = new LinkedHashSet<>();
        for (Map<String, Object> message : messages) {
            Object id = message.get("id");
            if (id instanceof String s && !s.isBlank()) {
                ids.add(s);
            }
        }
        return ids;
    }

    @SuppressWarnings("unchecked")
    private GoogleGmailNormalizedMessage fetchMessageDetails(String accessToken, String messageId) {
        Map<String, Object> response = restClient.get()
                .uri(
                        "/users/{userId}/messages/{id}?format=full&metadataHeaders=From&metadataHeaders=Subject&metadataHeaders=Date&metadataHeaders=To",
                        googleProperties.gmail().userId(),
                        messageId
                )
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        Map<String, Object> payload = (Map<String, Object>) response.get("payload");
        List<Map<String, String>> headers =
                payload == null ? List.of() : (List<Map<String, String>>) payload.getOrDefault("headers", List.of());
        String receivedHeader = getHeader(headers, "Date");
        String internalDateMillis = response.get("internalDate") == null ? "" : String.valueOf(response.get("internalDate"));
        String fromValue = getHeader(headers, "From");
        String[] sender = parseSender(fromValue);
        List<String> to = parseRecipients(getHeader(headers, "To"));
        List<String> labels = (List<String>) response.getOrDefault("labelIds", List.of());
        String bodyText = extractBodyText(payload);
        boolean read = !labels.contains("UNREAD");

        return new GoogleGmailNormalizedMessage(
                stringValue(response.get("id")),
                stringValue(response.get("threadId")),
                sender[0],
                sender[1],
                to,
                getHeader(headers, "Subject"),
                stringValue(response.get("snippet")),
                bodyText,
                parseReceivedAt(receivedHeader, internalDateMillis),
                stringValue(response.get("historyId")),
                read,
                labels
        );
    }

    private String fetchCurrentHistoryId(String accessToken) {
        Map<String, Object> profile = restClient.get()
                .uri("/users/{userId}/profile", googleProperties.gmail().userId())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);
        return profile == null ? "" : stringValue(profile.get("historyId"));
    }

    private String getHeader(List<Map<String, String>> headers, String name) {
        return headers.stream()
                .filter(header -> name.equalsIgnoreCase(header.get("name")))
                .map(header -> header.get("value"))
                .findFirst()
                .orElse("");
    }

    private OffsetDateTime parseReceivedAt(String dateHeader, String internalDateMillis) {
        if (dateHeader != null && !dateHeader.isBlank()) {
            try {
                return ZonedDateTime.parse(dateHeader, RFC_1123).toOffsetDateTime();
            } catch (Exception ignored) {
                try {
                    return OffsetDateTime.parse(dateHeader);
                } catch (Exception ignoredAgain) {
                    // Continue to internalDate fallback.
                }
            }
        }
        if (internalDateMillis != null && !internalDateMillis.isBlank()) {
            try {
                long epochMillis = Long.parseLong(internalDateMillis);
                return Instant.ofEpochMilli(epochMillis).atOffset(ZoneOffset.UTC);
            } catch (NumberFormatException ignored) {
                // Continue to now fallback.
            }
        }
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private String extractBodyText(Map<String, Object> payload) {
        if (payload == null) {
            return "";
        }
        Map<String, Object> body = (Map<String, Object>) payload.get("body");
        String data = body == null ? "" : stringValue(body.get("data"));
        if (!data.isBlank()) {
            return decodeBase64(data);
        }
        List<Map<String, Object>> parts = (List<Map<String, Object>>) payload.getOrDefault("parts", List.of());
        for (Map<String, Object> part : parts) {
            String mimeType = stringValue(part.get("mimeType"));
            if (!"text/plain".equalsIgnoreCase(mimeType)) {
                continue;
            }
            Map<String, Object> partBody = (Map<String, Object>) part.get("body");
            String partData = partBody == null ? "" : stringValue(partBody.get("data"));
            if (!partData.isBlank()) {
                return decodeBase64(partData);
            }
        }
        return "";
    }

    private String decodeBase64(String value) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(value);
            return new String(bytes);
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }

    private String[] parseSender(String from) {
        if (from == null || from.isBlank()) {
            return new String[]{"", ""};
        }
        int lt = from.indexOf('<');
        int gt = from.indexOf('>');
        if (lt >= 0 && gt > lt) {
            String name = from.substring(0, lt).trim().replace("\"", "");
            String email = from.substring(lt + 1, gt).trim();
            return new String[]{name, email};
        }
        return new String[]{"", from.trim()};
    }

    private List<String> parseRecipients(String to) {
        if (to == null || to.isBlank()) {
            return List.of();
        }
        return List.of(to.split(",")).stream().map(String::trim).map(this::extractEmail).toList();
    }

    private String extractEmail(String value) {
        int lt = value.indexOf('<');
        int gt = value.indexOf('>');
        if (lt >= 0 && gt > lt) {
            return value.substring(lt + 1, gt).trim();
        }
        return value;
    }
}
