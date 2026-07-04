package com.familyos.familyos.service;

import com.familyos.familyos.llm.LlmProvider;
import com.familyos.familyos.llm.LlmRequest;
import com.familyos.familyos.llm.LlmResponse;
import com.familyos.familyos.llm.factory.LlmProviderFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LlmService {

  private final LlmProviderFactory providerFactory;

  public LlmService(LlmProviderFactory providerFactory) {
    this.providerFactory = providerFactory;
  }

  public LlmResponse generate(String systemPrompt, String userPrompt) {
    return generate(null, "general", systemPrompt, userPrompt, Map.of());
  }

  public LlmResponse generate(String provider, String useCase, String systemPrompt, String userPrompt, Map<String, Object> metadata) {
    LlmProvider selectedProvider = providerFactory.providerOrDefault(provider);
    return selectedProvider.generate(new LlmRequest(
      useCase,
      systemPrompt,
      userPrompt,
      metadata
    ));
  }

  public LlmResponse test(String prompt) {
    return generate(null, "test", "You are a concise assistant.", prompt, Map.of());
  }

  public ProviderHealth checkHealth() {
    LlmProvider provider = providerFactory.defaultProvider();
    try {
      LlmResponse response = generate(
        provider.providerName(),
        "health-check",
        "Respond with just 'OK'.",
        "Are you working?",
        Map.of()
      );
      return new ProviderHealth(
        provider.providerName(),
        true,
        "Provider is healthy and responding",
        Map.of()
      );
    } catch (Exception e) {
      return new ProviderHealth(
        provider.providerName(),
        false,
        "Provider health check failed: " + e.getMessage(),
        Map.of("error", e.getMessage())
      );
    }
  }

  public record ProviderHealth(
    String provider,
    boolean healthy,
    String message,
    Map<String, Object> metadata
  ) {}
}
