package com.familyos.familyos.llm.openrouter;

import com.familyos.familyos.config.properties.OpenRouterProperties;
import com.familyos.familyos.llm.LlmRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenRouterClient {

  private final RestClient restClient;
  private final OpenRouterProperties properties;

  public OpenRouterClient(OpenRouterProperties properties) {
    this.properties = properties;
    this.restClient = RestClient.builder()
      .baseUrl(properties.baseUrl())
      .build();
  }

  public Map<String, Object> generateContent(LlmRequest request) {
    if (!StringUtils.hasText(properties.apiKey())) {
      throw new IllegalStateException("OPENROUTER_API_KEY is required to call the OpenRouter LLM provider.");
    }

    return restClient.post()
      .uri("/chat/completions")
      .header("Authorization", "Bearer " + properties.apiKey())
      .body(buildRequest(request))
      .retrieve()
      .body(Map.class);
  }

  private Map<String, Object> buildRequest(LlmRequest request) {
    List<Map<String, Object>> messages = List.of(
      Map.of(
        "role", "user",
        "content", request.userContent() == null ? "" : request.userContent()
      )
    );

    if (StringUtils.hasText(request.systemPrompt())) {
      messages = List.of(
        Map.of(
          "role", "system",
          "content", request.systemPrompt()
        ),
        Map.of(
          "role", "user",
          "content", request.userContent() == null ? "" : request.userContent()
        )
      );
    }

    return Map.of(
      "model", properties.model(),
      "messages", messages,
      "temperature", 0.7,
      "max_tokens", 1024
    );
  }
}
