package com.familyos.familyos.extraction.prompt;

import com.familyos.familyos.llm.LlmRequest;
import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class PromptBuilder {
    
    private final PromptLoader promptLoader;
    private static final String USE_CASE = "email-extraction";
    
    public PromptBuilder(PromptLoader promptLoader) {
        this.promptLoader = promptLoader;
    }
    
    public LlmRequest buildEmailExtractionRequest(NormalizedDocument document) {
        String systemPrompt = promptLoader.loadEmailExtractionPrompt();
        String userContent = buildEmailContent(document);
        
        return new LlmRequest(
            USE_CASE,
            systemPrompt,
            userContent,
            Map.of("documentId", document.id())
        );
    }
    
    private String buildEmailContent(NormalizedDocument document) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("From: ").append(document.sender()).append("\n");
        sb.append("Subject: ").append(document.subject()).append("\n");
        sb.append("Priority: ").append(document.priority()).append("\n");
        
        if (document.labels() != null && !document.labels().isEmpty()) {
            sb.append("Labels: ").append(String.join(", ", document.labels())).append("\n");
        }
        
        sb.append("\n");
        sb.append(document.content());
        
        return sb.toString();
    }
}
