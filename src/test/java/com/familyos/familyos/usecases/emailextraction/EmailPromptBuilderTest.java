package com.familyos.familyos.usecases.emailextraction;

import com.familyos.familyos.dto.GmailMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailPromptBuilderTest {

  private final EmailPromptBuilder builder = new EmailPromptBuilder(new ObjectMapper());

  @Test
  void buildUserPromptSerializesEmails() {
    String prompt = builder.buildUserPrompt(List.of(
      new GmailMessageDto("1", "thread-1", "school@example.com", "Homework update", "2026-07-03T00:00:00Z", "Please review the assignment")
    ));

    assertTrue(prompt.contains("Homework update"));
    assertTrue(prompt.contains("school@example.com"));
    assertTrue(prompt.contains("\"emails\""));
  }
}
