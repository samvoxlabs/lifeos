package com.familyos.familyos.llm.gemini;

import com.familyos.familyos.config.properties.GeminiProperties;
import com.familyos.familyos.llm.LlmProvider;
import com.familyos.familyos.llm.LlmRequest;
import com.familyos.familyos.llm.LlmResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GeminiProvider implements LlmProvider {

  private static final String PROVIDER_NAME = "gemini";

  private final GeminiClient client;
  private final GeminiProperties properties;

  public GeminiProvider(GeminiClient client, GeminiProperties properties) {
    this.client = client;
    this.properties = properties;
  }

  @Override
  public String providerName() {
    return PROVIDER_NAME;
  }

  @Override
  public LlmResponse generate(LlmRequest request) {
    Map<String, Object> response = client.generateContent(request);

    return new LlmResponse(
      PROVIDER_NAME,
      properties.model(),
      extractText(response),
      Map.of(
        "useCase", request.useCase(),
        "rawResponse", response == null ? Map.of() : response
      )
    );
  }

  private String extractText(Map<String, Object> response) {
    if (response == null) {
      return "";
    }

    List<Map<String, Object>> candidates =
      (List<Map<String, Object>>) response.getOrDefault("candidates", List.of());

    return candidates.stream()
      .findFirst()
      .map(candidate -> (Map<String, Object>) candidate.get("content"))
      .map(content -> (List<Map<String, Object>>) content.getOrDefault("parts", List.of()))
      .flatMap(parts -> parts.stream().findFirst())
      .map(part -> (String) part.getOrDefault("text", ""))
      .orElse("");
  }
}
