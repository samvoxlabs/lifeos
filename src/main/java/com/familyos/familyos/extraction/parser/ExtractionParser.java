package com.familyos.familyos.extraction.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.familyos.familyos.extraction.dto.ExtractionResult;
import com.familyos.familyos.extraction.exception.InvalidResponseException;
import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExtractionParser {
    
    private final ObjectMapper objectMapper;
    
    public ExtractionParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public ExtractionResult parse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new InvalidResponseException("Empty response from LLM");
        }
        
        String jsonContent = extractJsonFromResponse(response);
        
        try {
            return objectMapper.readValue(jsonContent, ExtractionResult.class);
        } catch (Exception e) {
            throw new InvalidResponseException("Failed to parse LLM response as JSON: " + e.getMessage(), e);
        }
    }
    
    private String extractJsonFromResponse(String response) {
        String trimmed = response.trim();
        
        // If response is already valid JSON, return it
        if (isValidJson(trimmed)) {
            return trimmed;
        }
        
        // Try to extract JSON from markdown code blocks
        Pattern codeBlockPattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```");
        Matcher matcher = codeBlockPattern.matcher(trimmed);
        if (matcher.find()) {
            String extracted = matcher.group(1).trim();
            if (isValidJson(extracted)) {
                return extracted;
            }
        }
        
        // Try to find JSON object boundaries
        int jsonStart = trimmed.indexOf('{');
        int jsonEnd = trimmed.lastIndexOf('}');
        
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            String extracted = trimmed.substring(jsonStart, jsonEnd + 1);
            if (isValidJson(extracted)) {
                return extracted;
            }
        }
        
        throw new InvalidResponseException("Could not extract valid JSON from LLM response");
    }
    
    private boolean isValidJson(String text) {
        try {
            objectMapper.readTree(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
