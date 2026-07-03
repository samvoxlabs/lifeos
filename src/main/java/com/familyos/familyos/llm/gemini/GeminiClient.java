package com.familyos.familyos.llm.gemini;

import com.familyos.familyos.config.properties.GeminiProperties;
import com.familyos.familyos.llm.LlmRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

  private final RestClient restClient;
  private final GeminiProperties properties;

  public GeminiClient(GeminiProperties properties) {
    this.properties = properties;
    this.restClient = RestClient.builder()
      .baseUrl(properties.baseUrl())
      .build();
  }

  public Map<String, Object> generateContent(LlmRequest request) {
    if (!StringUtils.hasText(properties.apiKey())) {
      throw new IllegalStateException("GEMINI_API_KEY is required to call the Gemini LLM provider.");
    }

    return restClient.post()
      .uri("/models/{model}:generateContent?key={apiKey}", properties.model(), properties.apiKey())
      .body(buildRequest(request))
      .retrieve()
      .body(Map.class);
  }

  private Map<String, Object> buildRequest(LlmRequest request) {
    Map<String, Object> body = Map.of(
      "contents", List.of(Map.of(
        "role", "user",
        "parts", List.of(Map.of("text", request.userContent() == null ? "" : request.userContent()))
      ))
    );

    if (!StringUtils.hasText(request.systemPrompt())) {
      return body;
    }

    return Map.of(
      "systemInstruction", Map.of(
        "parts", List.of(Map.of("text", request.systemPrompt()))
      ),
      "contents", body.get("contents")
    );
  }
}
