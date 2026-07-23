package com.familyos.familyos.controller;

import com.familyos.familyos.llm.LlmResponse;
import com.familyos.familyos.service.LlmService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping({"/api/llm", "/llm"})
public class LlmController {

  private final LlmService llmService;

  public LlmController(LlmService llmService) {
    this.llmService = llmService;
  }

  @PostMapping("/generate")
  public ApiLlmResponse generate(@RequestBody ApiLlmRequest request) {
    LlmResponse response = llmService.generate(
      request.provider(),
      "general",
      request.systemPrompt(),
      request.userPrompt(),
      Map.of()
    );
    return new ApiLlmResponse(
      response.provider(),
      response.model(),
      response.content()
    );
  }

  @GetMapping("/health")
  public HealthResponse health() {
    LlmService.ProviderHealth health = llmService.checkHealth();
    return new HealthResponse(
      health.provider(),
      health.healthy(),
      health.message()
    );
  }

  @GetMapping("/test")
  public LlmResponse test(@RequestParam(defaultValue = "Say hello") String prompt) {
    return llmService.test(prompt);
  }

  public record ApiLlmRequest(
    String systemPrompt,
    String userPrompt,
    String provider
  ) {}

  public record ApiLlmResponse(
    String provider,
    String model,
    String response
  ) {}

  public record HealthResponse(
    String provider,
    boolean healthy,
    String message
  ) {}
}
