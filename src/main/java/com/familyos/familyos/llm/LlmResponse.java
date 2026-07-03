package com.familyos.familyos.llm;

import java.util.Map;

public record LlmResponse(
  String provider,
  String model,
  String content,
  Map<String, Object> metadata
) {}
