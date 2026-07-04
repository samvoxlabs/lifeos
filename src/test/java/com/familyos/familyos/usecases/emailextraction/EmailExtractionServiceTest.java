package com.familyos.familyos.usecases.emailextraction;

import com.familyos.familyos.dto.GmailMessageDto;
import com.familyos.familyos.llm.LlmProvider;
import com.familyos.familyos.llm.LlmRequest;
import com.familyos.familyos.llm.LlmResponse;
import com.familyos.familyos.llm.factory.LlmProviderFactory;
import com.familyos.familyos.service.GmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailExtractionServiceTest {

  @Mock
  private GmailService gmailService;

  @Mock
  private EmailPromptBuilder promptBuilder;

  @Mock
  private EmailExtractionParser parser;

  @Mock
  private LlmProviderFactory providerFactory;

  @Mock
  private LlmProvider provider;

  private EmailExtractionService service;

  @BeforeEach
  void setUp() {
    service = new EmailExtractionService(gmailService, promptBuilder, parser, providerFactory);
  }

  @Test
  void extractBuildsPromptAndParsesResponse() {
    List<GmailMessageDto> emails = List.of(
      new GmailMessageDto("1", "thread-1", "school@example.com", "Homework update", "2026-07-03T00:00:00Z", "Please review the assignment")
    );

    when(gmailService.readAllowedMessages("user-id")).thenReturn(emails);
    when(promptBuilder.systemPrompt()).thenReturn("system");
    when(promptBuilder.buildUserPrompt(emails)).thenReturn("prompt");
    when(providerFactory.providerOrDefault(null)).thenReturn(provider);
    when(provider.generate(any(LlmRequest.class))).thenReturn(new LlmResponse("gemini", "model", "{\"emails\":[]}", Map.of()));
    when(parser.parse(any(LlmResponse.class))).thenReturn(new EmailExtractionResponse(List.of()));

    EmailExtractionResponse response = service.extract("user-id", null);

    assertEquals(0, response.emails().size());
    verify(gmailService).readAllowedMessages("user-id");
    verify(provider).generate(any(LlmRequest.class));
    verify(parser).parse(any(LlmResponse.class));
  }
}
