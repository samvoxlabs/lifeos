package com.familyos.familyos.integrations.google.gmail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class GoogleGmailClientImpl implements GoogleGmailClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleGmailClientImpl.class);
    private static final String GMAIL_API_BASE_URL = "https://gmail.googleapis.com/gmail/v1";

    private final RestClient restClient;

    public GoogleGmailClientImpl() {
        this.restClient = RestClient.builder()
                .baseUrl(GMAIL_API_BASE_URL)
                .build();
    }

    @Override
    public List<GoogleGmailMessage> fetchMessages(String accessToken, int maxResults) {
        log.debug("Fetching {} messages from Gmail API", maxResults);

        Map<String, Object> listResponse = restClient.get()
                .uri("/users/me/messages?maxResults={maxResults}", maxResults)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> messages =
                (List<Map<String, Object>>) listResponse.getOrDefault("messages", List.of());

        log.debug("Retrieved {} message summaries from Gmail API", messages.size());

        return messages.stream()
                .map(message -> fetchMessageDetails((String) message.get("id"), accessToken))
                .toList();
    }

    private GoogleGmailMessage fetchMessageDetails(String messageId, String accessToken) {
        log.debug("Fetching details for message: {}", messageId);

        Map<String, Object> response = restClient.get()
                .uri("/users/me/messages/{id}?format=metadata&metadataHeaders=From&metadataHeaders=Subject&metadataHeaders=Date",
                        messageId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        Map<String, Object> payload = (Map<String, Object>) response.get("payload");
        List<Map<String, String>> headers =
                (List<Map<String, String>>) payload.getOrDefault("headers", List.of());

        return new GoogleGmailMessage(
                (String) response.get("id"),
                (String) response.get("threadId"),
                getHeader(headers, "From"),
                getHeader(headers, "Subject"),
                getHeader(headers, "Date"),
                (String) response.get("snippet")
        );
    }

    private String getHeader(List<Map<String, String>> headers, String name) {
        return headers.stream()
                .filter(header -> name.equalsIgnoreCase(header.get("name")))
                .map(header -> header.get("value"))
                .findFirst()
                .orElse("");
    }
}
