package com.familyos.familyos.usecases.emailextraction;

import com.familyos.familyos.llm.LlmResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailExtractionParser {

  private final ObjectMapper objectMapper;

  public EmailExtractionParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public EmailExtractionResponse parse(LlmResponse response) {
    String content = response == null ? null : response.content();
    if (content == null || content.isBlank()) {
      throw new IllegalStateException("LLM returned an empty email extraction response");
    }

    try {
      JsonNode root = objectMapper.readTree(stripCodeFences(content));
      if (root.isArray()) {
        List<EmailExtractionResponse.ExtractedEmail> emails = objectMapper.convertValue(
          root,
          new TypeReference<List<EmailExtractionResponse.ExtractedEmail>>() {}
        );
        return new EmailExtractionResponse(emails);
      }
      JsonNode emailsNode = root.get("emails");
      if (emailsNode != null && emailsNode.isArray()) {
        return objectMapper.treeToValue(root, EmailExtractionResponse.class);
      }
      throw new IllegalStateException("LLM response did not contain an emails array");
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to parse email extraction JSON", ex);
    }
  }

  private String stripCodeFences(String content) {
    String trimmed = content.trim();
    if (!trimmed.startsWith("```")) {
      return trimmed;
    }

    int firstNewline = trimmed.indexOf('\n');
    if (firstNewline < 0) {
      return trimmed;
    }

    String body = trimmed.substring(firstNewline + 1);
    if (body.endsWith("```")) {
      body = body.substring(0, body.length() - 3);
    }
    return body.trim();
  }
}
