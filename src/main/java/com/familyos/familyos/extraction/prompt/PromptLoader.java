package com.familyos.familyos.extraction.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class PromptLoader {
    
    public String loadEmailExtractionPrompt() {
        return loadPrompt("prompts/email-extraction.txt");
    }
    
    public String loadPrompt(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load prompt: " + resourcePath, e);
        }
    }
}
