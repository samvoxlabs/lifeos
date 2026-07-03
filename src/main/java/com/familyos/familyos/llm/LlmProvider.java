package com.familyos.familyos.llm;

public interface LlmProvider {

  String providerName();

  LlmResponse generate(LlmRequest request);
}
