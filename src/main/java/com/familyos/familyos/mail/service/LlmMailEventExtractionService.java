package com.familyos.familyos.mail.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.familyos.familyos.llm.LlmResponse;
import com.familyos.familyos.mail.entity.MailMessage;
import com.familyos.familyos.mail.model.ExtractedMailEventCandidate;
import com.familyos.familyos.service.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LlmMailEventExtractionService {

    private static final Logger log = LoggerFactory.getLogger(LlmMailEventExtractionService.class);

    private final LlmService llmService;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;

    public LlmMailEventExtractionService(LlmService llmService, ObjectMapper objectMapper) {
        this.llmService = llmService;
        this.objectMapper = objectMapper;
        this.systemPrompt = loadPrompt();
    }

    @SuppressWarnings("unchecked")
    public Optional<ExtractedMailEventCandidate> extractFromMessage(MailMessage message) {
        try {
            String emailContent = buildEmailContent(message);
            LlmResponse response = llmService.generate(systemPrompt, emailContent);
            String json = response.content().trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("```[a-z]*\\n?", "").replaceAll("```", "").trim();
            }
            Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
            Boolean hasEvent = (Boolean) parsed.getOrDefault("hasEvent", false);
            if (!hasEvent || !parsed.containsKey("event") || parsed.get("event") == null) {
                return Optional.empty();
            }
            Map<String, Object> eventMap = (Map<String, Object>) parsed.get("event");
            String title = (String) eventMap.getOrDefault("title", message.getSubject());
            String startStr = (String) eventMap.get("start");
            String endStr = (String) eventMap.get("end");
            String location = (String) eventMap.get("location");
            String description = (String) eventMap.get("description");
            String type = (String) eventMap.getOrDefault("type", "other");
            String category = (String) eventMap.getOrDefault("category", "other");
            String priority = (String) eventMap.getOrDefault("priority", "medium");
            double confidence = toDouble(eventMap.getOrDefault("confidence", 0.85));

            OffsetDateTime start = parseDateTime(startStr, message.getReceivedAt());
            OffsetDateTime end = endStr != null ? parseDateTime(endStr, start.plusHours(1)) : start.plusHours(1);

            return Optional.of(new ExtractedMailEventCandidate(title, start, end, location, description, type, category, priority, confidence));
        } catch (Exception e) {
            log.warn("LLM extraction failed for message {}: {}", message.getGmailMessageId(), e.getMessage());
            return Optional.empty();
        }
    }

    private String buildEmailContent(MailMessage message) {
        StringBuilder sb = new StringBuilder();
        sb.append("From: ");
        if (message.getSenderName() != null) sb.append(message.getSenderName()).append(" ");
        if (message.getSenderEmail() != null) sb.append("<").append(message.getSenderEmail()).append(">");
        sb.append("\n");
        sb.append("Subject: ").append(message.getSubject()).append("\n");
        sb.append("Date: ").append(message.getReceivedAt()).append("\n\n");
        if (message.getBodyText() != null && !message.getBodyText().isBlank()) {
            sb.append(message.getBodyText());
        } else if (message.getSnippet() != null) {
            sb.append(message.getSnippet());
        }
        return sb.toString();
    }

    private OffsetDateTime parseDateTime(String s, OffsetDateTime fallback) {
        if (s == null || s.isBlank()) return fallback;
        try {
            return OffsetDateTime.parse(s);
        } catch (DateTimeParseException e) {
            return fallback;
        }
    }

    private double toDouble(Object val) {
        if (val instanceof Number n) return n.doubleValue();
        return 0.85;
    }

    private String loadPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/event-extraction-v2.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            return "You are an AI that extracts calendar events from emails. Return JSON with hasEvent and event fields.";
        }
    }
}
