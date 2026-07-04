package com.familyos.familyos.extraction.parser;

import com.familyos.familyos.extraction.dto.ActionCandidate;
import com.familyos.familyos.extraction.dto.ExtractionResult;
import com.familyos.familyos.extraction.exception.InvalidResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExtractionParserTest {
    
    private ExtractionParser parser;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        parser = new ExtractionParser(objectMapper);
    }
    
    @Test
    void testParseValidJson() {
        String validJson = """
            {
              "summary": "Team meeting scheduled",
              "confidence": 0.95,
              "actions": [
                {
                  "type": "EVENT",
                  "title": "Team Meeting",
                  "description": "Weekly sync",
                  "dueDate": "2025-06-15T10:00:00"
                }
              ]
            }
            """;
        
        ExtractionResult result = parser.parse(validJson);
        
        assertNotNull(result);
        assertEquals("Team meeting scheduled", result.summary());
        assertEquals(0.95, result.confidence());
        assertEquals(1, result.actions().size());
        assertEquals("EVENT", result.actions().get(0).type());
        assertEquals("Team Meeting", result.actions().get(0).title());
    }
    
    @Test
    void testParseJsonInMarkdownCodeBlock() {
        String jsonWithMarkdown = """
            ```json
            {
              "summary": "Test extraction",
              "confidence": 0.88,
              "actions": []
            }
            ```
            """;
        
        ExtractionResult result = parser.parse(jsonWithMarkdown);
        
        assertNotNull(result);
        assertEquals("Test extraction", result.summary());
        assertEquals(0.88, result.confidence());
    }
    
    @Test
    void testParseJsonWithTextAround() {
        String jsonWithText = """
            Here is the extracted information:
            
            {
              "summary": "Important reminder",
              "confidence": 0.92,
              "actions": [
                {
                  "type": "TASK",
                  "title": "Complete report"
                }
              ]
            }
            
            End of extraction.
            """;
        
        ExtractionResult result = parser.parse(jsonWithText);
        
        assertNotNull(result);
        assertEquals("Important reminder", result.summary());
        assertEquals("Complete report", result.actions().get(0).title());
    }
    
    @Test
    void testParseMultipleActions() {
        String jsonWithMultipleActions = """
            {
              "summary": "Meeting and task",
              "confidence": 0.90,
              "actions": [
                {
                  "type": "EVENT",
                  "title": "Board Meeting",
                  "dueDate": "2025-06-20T14:00:00"
                },
                {
                  "type": "TASK",
                  "title": "Prepare presentation"
                },
                {
                  "type": "REMINDER",
                  "title": "Send follow-up email"
                }
              ]
            }
            """;
        
        ExtractionResult result = parser.parse(jsonWithMultipleActions);
        
        assertEquals(3, result.actions().size());
        assertTrue(result.actions().stream().anyMatch(a -> "EVENT".equals(a.type())));
        assertTrue(result.actions().stream().anyMatch(a -> "TASK".equals(a.type())));
        assertTrue(result.actions().stream().anyMatch(a -> "REMINDER".equals(a.type())));
    }
    
    @Test
    void testParseEmptyResponse() {
        assertThrows(InvalidResponseException.class, () -> parser.parse(""));
    }
    
    @Test
    void testParseNullResponse() {
        assertThrows(InvalidResponseException.class, () -> parser.parse(null));
    }
    
    @Test
    void testParseInvalidJson() {
        String invalidJson = "{ this is not valid json }";
        
        assertThrows(InvalidResponseException.class, () -> parser.parse(invalidJson));
    }
    
    @Test
    void testParseNoJsonFound() {
        String textWithoutJson = "This text contains no JSON at all.";
        
        assertThrows(InvalidResponseException.class, () -> parser.parse(textWithoutJson));
    }
    
    @Test
    void testParseJsonWithOptionalFields() {
        String jsonWithOptionalFields = """
            {
              "summary": "Meeting",
              "confidence": 0.85,
              "actions": [
                {
                  "type": "EVENT",
                  "title": "Meeting"
                }
              ]
            }
            """;
        
        ExtractionResult result = parser.parse(jsonWithOptionalFields);
        
        assertNotNull(result);
        ActionCandidate action = result.actions().get(0);
        assertNull(action.description());
        assertNull(action.dueDate());
    }
}
