package com.familyos.familyos.extraction.prompt;

import com.familyos.familyos.llm.LlmRequest;
import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderTest {
    
    private PromptBuilder promptBuilder;
    private PromptLoader promptLoader;
    
    @BeforeEach
    void setUp() {
        promptLoader = new PromptLoader();
        promptBuilder = new PromptBuilder(promptLoader);
    }
    
    @Test
    void testBuildEmailExtractionRequest() {
        NormalizedDocument document = new NormalizedDocument(
            "doc-1",
            "John Doe",
            "Team Meeting Tomorrow",
            "We have a team meeting scheduled for tomorrow at 2 PM.",
            List.of("work", "meeting"),
            "1",
            "email"
        );
        
        LlmRequest request = promptBuilder.buildEmailExtractionRequest(document);
        
        assertNotNull(request);
        assertEquals("email-extraction", request.useCase());
        assertNotNull(request.systemPrompt());
        assertFalse(request.systemPrompt().isEmpty());
        assertNotNull(request.userContent());
        assertFalse(request.userContent().isEmpty());
    }
    
    @Test
    void testUserContentContainsEmailHeaders() {
        NormalizedDocument document = new NormalizedDocument(
            "doc-2",
            "Alice Smith",
            "Project Deadline",
            "The project deadline is next Friday.",
            List.of("project"),
            "1",
            "email"
        );
        
        LlmRequest request = promptBuilder.buildEmailExtractionRequest(document);
        String userContent = request.userContent();
        
        assertTrue(userContent.contains("From: Alice Smith"));
        assertTrue(userContent.contains("Subject: Project Deadline"));
        assertTrue(userContent.contains("Priority: 1"));
        assertTrue(userContent.contains("Labels: project"));
        assertTrue(userContent.contains("The project deadline is next Friday."));
    }
    
    @Test
    void testUserContentWithMultipleLabels() {
        NormalizedDocument document = new NormalizedDocument(
            "doc-3",
            "Bob Johnson",
            "Urgent Action Required",
            "Please respond immediately.",
            List.of("urgent", "action", "work"),
            "2",
            "email"
        );
        
        LlmRequest request = promptBuilder.buildEmailExtractionRequest(document);
        String userContent = request.userContent();
        
        assertTrue(userContent.contains("Labels: urgent, action, work"));
    }
    
    @Test
    void testUserContentWithNoLabels() {
        NormalizedDocument document = new NormalizedDocument(
            "doc-4",
            "Carol White",
            "Simple Email",
            "This is a simple email.",
            List.of(),
            "0",
            "email"
        );
        
        LlmRequest request = promptBuilder.buildEmailExtractionRequest(document);
        String userContent = request.userContent();
        
        assertTrue(userContent.contains("From: Carol White"));
        assertTrue(userContent.contains("Subject: Simple Email"));
        assertFalse(userContent.contains("Labels:"));
    }
    
    @Test
    void testMetadataContainsDocumentId() {
        NormalizedDocument document = new NormalizedDocument(
            "doc-5",
            "Dave Brown",
            "Test Email",
            "Test content",
            List.of(),
            "1",
            "email"
        );
        
        LlmRequest request = promptBuilder.buildEmailExtractionRequest(document);
        
        assertTrue(request.metadata().containsKey("documentId"));
        assertEquals("doc-5", request.metadata().get("documentId"));
    }
}
