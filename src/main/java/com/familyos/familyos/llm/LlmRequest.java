package com.familyos.familyos.llm;

import java.util.Map;

public record LlmRequest(
  String useCase,
  String systemPrompt,
  String userContent,
  Map<String, Object> metadata
) {}
