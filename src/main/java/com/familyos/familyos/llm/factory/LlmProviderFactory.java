package com.familyos.familyos.llm.factory;

import com.familyos.familyos.llm.LlmProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LlmProviderFactory {

  private final String defaultProvider;
  private final Map<String, LlmProvider> providers;

  public LlmProviderFactory(
    @Value("${llm.default-provider:gemini}") String defaultProvider,
    List<LlmProvider> providers
  ) {
    this.defaultProvider = normalize(defaultProvider);
    this.providers = providers.stream()
      .collect(Collectors.toUnmodifiableMap(
        provider -> normalize(provider.providerName()),
        Function.identity()
      ));
  }

  public LlmProvider defaultProvider() {
    return provider(defaultProvider);
  }

  public LlmProvider providerOrDefault(String providerName) {
    if (providerName == null || providerName.isBlank()) {
      return defaultProvider();
    }
    return provider(providerName);
  }

  public LlmProvider provider(String providerName) {
    String normalizedProviderName = normalize(providerName);
    LlmProvider provider = providers.get(normalizedProviderName);
    if (provider == null) {
      throw new IllegalArgumentException("Unsupported LLM provider: " + providerName);
    }
    return provider;
  }

  private String normalize(String providerName) {
    return providerName == null ? "" : providerName.trim().toLowerCase(Locale.ROOT);
  }
}
