package com.familyos.familyos.usecases.emailextraction;

import com.familyos.familyos.llm.LlmResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailExtractionParserTest {

  private final EmailExtractionParser parser = new EmailExtractionParser(new ObjectMapper());

  @Test
  void parseExtractsEmailDtosFromJsonObject() {
    EmailExtractionResponse response = parser.parse(new LlmResponse(
      "gemini",
      "model",
      """
      {
        "emails": [
          {
            "subject": "Homework update",
            "summary": "Review the assignment",
            "category": "SCHOOL",
            "priority": "HIGH",
            "actionItems": [
              {
                "title": "Review assignment",
                "dueDate": "2026-07-10"
              }
            ],
            "events": [
              {
                "title": "Parent meeting",
                "date": "2026-07-12"
              }
            ],
            "people": ["Teacher"],
            "followUps": ["Reply by Friday"]
          }
        ]
      }
      """,
      Map.of()
    ));

    assertEquals(1, response.emails().size());
    assertEquals("Homework update", response.emails().get(0).subject());
    assertEquals(1, response.emails().get(0).actionItems().size());
    assertEquals("Review assignment", response.emails().get(0).actionItems().get(0).title());
  }

  @Test
  void parseAcceptsRootArrayPayloads() {
    EmailExtractionResponse response = parser.parse(new LlmResponse(
      "gemini",
      "model",
      """
      [
        {
          "subject": "Invoice",
          "summary": "Pay the bill",
          "category": "FINANCE",
          "priority": "HIGH"
        }
      ]
      """,
      Map.of()
    ));

    assertEquals(1, response.emails().size());
    assertEquals("Invoice", response.emails().get(0).subject());
  }
}
