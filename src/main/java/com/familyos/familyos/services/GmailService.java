package com.familyos.familyos.services;

import com.familyos.familyos.dto.GmailMessageDto;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GmailService {

  private final RestClient restClient;
  private final OAuth2AuthorizedClientService authorizedClientService;

  public GmailService(OAuth2AuthorizedClientService authorizedClientService) {
    this.authorizedClientService = authorizedClientService;
    this.restClient = RestClient.builder()
      .baseUrl("https://gmail.googleapis.com/gmail/v1")
      .build();
  }

  public List<GmailMessageDto> readLatestMessages(OAuth2AuthenticationToken authentication) {
    OAuth2AuthorizedClient client =
      authorizedClientService.loadAuthorizedClient(
        authentication.getAuthorizedClientRegistrationId(),
        authentication.getName()
      );

    String accessToken = client.getAccessToken().getTokenValue();

    Map<String, Object> listResponse = restClient.get()
      .uri("/users/me/messages?maxResults=10")
      .header("Authorization", "Bearer " + accessToken)
      .retrieve()
      .body(Map.class);

    List<Map<String, Object>> messages =
      (List<Map<String, Object>>) listResponse.getOrDefault("messages", List.of());

    return messages.stream()
      .map(message -> getMessageDetails((String) message.get("id"), accessToken))
      .toList();
  }

  private GmailMessageDto getMessageDetails(String messageId, String accessToken) {
    Map<String, Object> response = restClient.get()
      .uri("/users/me/messages/{id}?format=metadata&metadataHeaders=From&metadataHeaders=Subject&metadataHeaders=Date", messageId)
      .header("Authorization", "Bearer " + accessToken)
      .retrieve()
      .body(Map.class);

    Map<String, Object> payload = (Map<String, Object>) response.get("payload");
    List<Map<String, String>> headers =
      (List<Map<String, String>>) payload.getOrDefault("headers", List.of());

    return new GmailMessageDto(
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
