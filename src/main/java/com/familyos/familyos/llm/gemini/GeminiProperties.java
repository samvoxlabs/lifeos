package com.familyos.familyos.llm.gemini;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm.gemini")
public record GeminiProperties(
  String apiKey,
  String model,
  String baseUrl
) {}
