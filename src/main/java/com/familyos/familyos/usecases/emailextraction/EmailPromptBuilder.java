package com.familyos.familyos.usecases.emailextraction;

import com.familyos.familyos.dto.GmailMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailPromptBuilder {

  private static final String SYSTEM_PROMPT = """
    You are the AI engine for LifeOS.

    Analyze the supplied emails.

    Return ONLY valid JSON.

    Do not include Markdown.

    Do not include explanations.

    Extract only meaningful information.
    """;

  private static final String RESPONSE_SCHEMA = """
    {
      "emails": [
        {
          "subject": "...",
          "from": "...",
          "summary": "...",
          "category": "...",
          "priority": "...",
          "actionItems": [
            {
              "title": "...",
              "dueDate": "..."
            }
          ],
          "events": [
            {
              "title": "...",
              "date": "..."
            }
          ],
          "people": ["..."],
          "followUps": ["..."]
        }
      ]
    }
    """;

  private final ObjectMapper objectMapper;

  public EmailPromptBuilder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String systemPrompt() {
    return SYSTEM_PROMPT;
  }

  public String buildUserPrompt(List<GmailMessageDto> emails) {
    try {
      String emailJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
        emails.stream()
          .map(email -> new PromptEmail(
            email.subject(),
            email.from(),
            email.date(),
            email.snippet()
          ))
          .toList()
      );

      return """
        Extract structured LifeOS knowledge from these emails.

        Return JSON using this shape:
        %s

        Emails:
        %s
        """.formatted(RESPONSE_SCHEMA, emailJson);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to build email extraction prompt", ex);
    }
  }

  private record PromptEmail(
    String subject,
    String from,
    String date,
    String snippet
  ) {}
}
