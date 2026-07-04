package com.familyos.familyos.llm.groq;

import com.familyos.familyos.config.properties.GroqProperties;
import com.familyos.familyos.llm.LlmProvider;
import com.familyos.familyos.llm.LlmRequest;
import com.familyos.familyos.llm.LlmResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GroqProvider implements LlmProvider {

  private static final String PROVIDER_NAME = "groq";

  private final GroqClient client;
  private final GroqProperties properties;

  public GroqProvider(GroqClient client, GroqProperties properties) {
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

    List<Map<String, Object>> choices =
      (List<Map<String, Object>>) response.getOrDefault("choices", List.of());

    return choices.stream()
      .findFirst()
      .map(choice -> (Map<String, Object>) choice.get("message"))
      .map(message -> (String) message.getOrDefault("content", ""))
      .orElse("");
  }
}
