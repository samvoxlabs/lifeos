package com.familyos.familyos.extraction.service;

import com.familyos.familyos.extraction.dto.ExtractionResponse;
import com.familyos.familyos.extraction.dto.ExtractionResult;
import com.familyos.familyos.extraction.parser.ExtractionParser;
import com.familyos.familyos.extraction.prompt.PromptBuilder;
import com.familyos.familyos.llm.LlmRequest;
import com.familyos.familyos.llm.LlmResponse;
import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.ruleengine.dto.RuleDecision;
import com.familyos.familyos.ruleengine.dto.RuleResult;
import com.familyos.familyos.ruleengine.service.RuleEngineService;
import com.familyos.familyos.service.LlmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtractionServiceTest {
    
    @Mock
    private RuleEngineService ruleEngineService;
    
    @Mock
    private PromptBuilder promptBuilder;
    
    @Mock
    private LlmService llmService;
    
    @Mock
    private ExtractionParser parser;
    
    private ExtractionService extractionService;
    private NormalizedDocument testDocument;
    
    @BeforeEach
    void setUp() {
        extractionService = new ExtractionService(
            ruleEngineService,
            promptBuilder,
            llmService,
            parser
        );
        
        testDocument = new NormalizedDocument(
            "doc-1",
            "test@example.com",
            "Test Subject",
            "Test content",
            List.of(),
            "1",
            "email"
        );
    }
    
    @Test
    void testProcessDocumentWithProcessDecision() {
        // Setup
        RuleResult ruleResult = new RuleResult(RuleDecision.PROCESS, "default", "Should process", 100);
        when(ruleEngineService.evaluate(testDocument)).thenReturn(ruleResult);
        
        LlmRequest mockRequest = new LlmRequest("email-extraction", "system", "user", Map.of());
        when(promptBuilder.buildEmailExtractionRequest(testDocument)).thenReturn(mockRequest);
        
        LlmResponse mockResponse = new LlmResponse("gemini", "gemini-pro", 
            """
            {
              "summary": "Test extraction",
              "confidence": 0.9,
              "actions": []
            }
            """, Map.of());
        when(llmService.generate(isNull(), anyString(), anyString(), anyString(), anyMap()))
            .thenReturn(mockResponse);
        
        ExtractionResult mockResult = new ExtractionResult("Test extraction", 0.9, List.of());
        when(parser.parse(anyString())).thenReturn(mockResult);
        
        // Execute
        ExtractionResponse response = extractionService.process(testDocument);
        
        // Verify
        assertNotNull(response);
        assertEquals("SUCCESS", response.status());
        assertEquals(mockResult, response.result());
        verify(ruleEngineService).evaluate(testDocument);
        verify(promptBuilder).buildEmailExtractionRequest(testDocument);
        verify(llmService).generate(isNull(), anyString(), anyString(), anyString(), anyMap());
        verify(parser).parse(anyString());
    }
    
    @Test
    void testProcessDocumentWithIgnoreDecision() {
        // Setup
        RuleResult ruleResult = new RuleResult(RuleDecision.IGNORE, "default", "Not relevant", 0);
        when(ruleEngineService.evaluate(testDocument)).thenReturn(ruleResult);
        
        // Execute
        ExtractionResponse response = extractionService.process(testDocument);
        
        // Verify
        assertNotNull(response);
        assertEquals("SKIPPED", response.status());
        verify(ruleEngineService).evaluate(testDocument);
        verify(promptBuilder, never()).buildEmailExtractionRequest(any());
        verify(llmService, never()).generate(isNull(), anyString(), anyString(), anyString(), anyMap());
        verify(parser, never()).parse(anyString());
    }
    
    @Test
    void testProcessDocumentWithLowPriorityDecision() {
        // Setup
        RuleResult ruleResult = new RuleResult(RuleDecision.LOW_PRIORITY, "default", "Low priority", 25);
        when(ruleEngineService.evaluate(testDocument)).thenReturn(ruleResult);
        
        // Execute
        ExtractionResponse response = extractionService.process(testDocument);
        
        // Verify
        assertNotNull(response);
        assertEquals("SKIPPED", response.status());
        verify(llmService, never()).generate(isNull(), anyString(), anyString(), anyString(), anyMap());
    }
    
    @Test
    void testProcessDocumentWithManualReviewDecision() {
        // Setup
        RuleResult ruleResult = new RuleResult(RuleDecision.MANUAL_REVIEW, "default", "Needs review", 50);
        when(ruleEngineService.evaluate(testDocument)).thenReturn(ruleResult);
        
        // Execute
        ExtractionResponse response = extractionService.process(testDocument);
        
        // Verify
        assertNotNull(response);
        assertEquals("SKIPPED", response.status());
        verify(llmService, never()).generate(isNull(), anyString(), anyString(), anyString(), anyMap());
    }
    
    @Test
    void testProcessDocumentLlmException() {
        // Setup
        RuleResult ruleResult = new RuleResult(RuleDecision.PROCESS, "default", "Should process", 100);
        when(ruleEngineService.evaluate(testDocument)).thenReturn(ruleResult);
        
        LlmRequest mockRequest = new LlmRequest("email-extraction", "system", "user", Map.of());
        when(promptBuilder.buildEmailExtractionRequest(testDocument)).thenReturn(mockRequest);
        
        when(llmService.generate(isNull(), anyString(), anyString(), anyString(), anyMap()))
            .thenThrow(new RuntimeException("LLM Service Error"));
        
        // Execute
        ExtractionResponse response = extractionService.process(testDocument);
        
        // Verify
        assertNotNull(response);
        assertEquals("ERROR", response.status());
        assertTrue(response.message().contains("Extraction failed"));
    }
    
    @Test
    void testProcessDocumentParserException() {
        // Setup
        RuleResult ruleResult = new RuleResult(RuleDecision.PROCESS, "default", "Should process", 100);
        when(ruleEngineService.evaluate(testDocument)).thenReturn(ruleResult);
        
        LlmRequest mockRequest = new LlmRequest("email-extraction", "system", "user", Map.of());
        when(promptBuilder.buildEmailExtractionRequest(testDocument)).thenReturn(mockRequest);
        
        LlmResponse mockResponse = new LlmResponse("gemini", "gemini-pro", 
            "Invalid JSON", Map.of());
        when(llmService.generate(isNull(), anyString(), anyString(), anyString(), anyMap()))
            .thenReturn(mockResponse);
        
        when(parser.parse(anyString())).thenThrow(new RuntimeException("Parse error"));
        
        // Execute
        ExtractionResponse response = extractionService.process(testDocument);
        
        // Verify
        assertNotNull(response);
        assertEquals("ERROR", response.status());
    }
}
