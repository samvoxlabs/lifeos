package com.familyos.familyos.controller;

import com.familyos.familyos.llm.LlmRequest;
import com.familyos.familyos.llm.LlmResponse;
import com.familyos.familyos.llm.factory.LlmProviderFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/llm")
public class LlmController {

  private final LlmProviderFactory providerFactory;

  public LlmController(LlmProviderFactory providerFactory) {
    this.providerFactory = providerFactory;
  }

  @PostMapping("/generate")
  public LlmResponse generate(@RequestBody LlmGenerateRequest request) {
    return providerFactory.providerOrDefault(request.provider()).generate(new LlmRequest(
      request.useCase() == null ? "general" : request.useCase(),
      request.systemPrompt(),
      request.content(),
      request.metadata() == null ? Map.of() : request.metadata()
    ));
  }

  @GetMapping("/test")
  public LlmResponse test(@RequestParam(defaultValue = "Say hello") String prompt) {
    return providerFactory.providerOrDefault(null).generate(new LlmRequest(
      "test",
      "You are a concise assistant.",
      prompt,
      Map.of()
    ));
  }

  public record LlmGenerateRequest(
    String provider,
    String useCase,
    String systemPrompt,
    String content,
    Map<String, Object> metadata
  ) {}
}
