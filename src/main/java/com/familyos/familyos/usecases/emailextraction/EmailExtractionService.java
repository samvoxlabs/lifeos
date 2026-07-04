package com.familyos.familyos.usecases.emailextraction;

import com.familyos.familyos.dto.GmailMessageDto;
import com.familyos.familyos.llm.LlmRequest;
import com.familyos.familyos.llm.LlmResponse;
import com.familyos.familyos.llm.factory.LlmProviderFactory;
import com.familyos.familyos.service.GmailService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EmailExtractionService {

  private static final String USE_CASE = "email-extraction";

  private final GmailService gmailService;
  private final EmailPromptBuilder promptBuilder;
  private final EmailExtractionParser parser;
  private final LlmProviderFactory providerFactory;

  public EmailExtractionService(
    GmailService gmailService,
    EmailPromptBuilder promptBuilder,
    EmailExtractionParser parser,
    LlmProviderFactory providerFactory
  ) {
    this.gmailService = gmailService;
    this.promptBuilder = promptBuilder;
    this.parser = parser;
    this.providerFactory = providerFactory;
  }

  public EmailExtractionResponse extract(String userId, EmailExtractionRequest request) {
    List<GmailMessageDto> emails = gmailService.readAllowedMessages(userId);
    LlmRequest llmRequest = new LlmRequest(
      USE_CASE,
      promptBuilder.systemPrompt(),
      promptBuilder.buildUserPrompt(emails),
      Map.of(
        "userId", userId,
        "messageCount", emails.size()
      )
    );

    LlmResponse response = providerFactory.providerOrDefault(request == null ? null : request.provider()).generate(llmRequest);
    return parser.parse(response);
  }
}
