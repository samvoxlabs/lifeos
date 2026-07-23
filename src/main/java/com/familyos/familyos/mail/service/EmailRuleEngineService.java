package com.familyos.familyos.mail.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.familyos.familyos.mail.entity.MailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class EmailRuleEngineService {

    private static final Logger log = LoggerFactory.getLogger(EmailRuleEngineService.class);

    private final List<String> allowedSenders;
    private final List<String> keywords;
    private final List<String> ignoreCategories;
    private final List<String> ignoreSubjectPatterns;

    @SuppressWarnings("unchecked")
    public EmailRuleEngineService(ObjectMapper objectMapper) {
        try {
            ClassPathResource resource = new ClassPathResource("config/email-rules.json");
            Map<String, Object> rules = objectMapper.readValue(resource.getInputStream(), Map.class);
            this.allowedSenders = (List<String>) rules.getOrDefault("allowedSenders", List.of());
            this.keywords = (List<String>) rules.getOrDefault("keywords", List.of());
            this.ignoreCategories = (List<String>) rules.getOrDefault("ignoreCategories", List.of());
            this.ignoreSubjectPatterns = (List<String>) rules.getOrDefault("ignoreSubjectPatterns", List.of());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email-rules.json", e);
        }
    }

    public boolean isRelevant(MailMessage message) {
        String subject = blankToEmpty(message.getSubject()).toLowerCase(Locale.ROOT);
        String body = blankToEmpty(message.getBodyText()).toLowerCase(Locale.ROOT);
        String senderEmail = blankToEmpty(message.getSenderEmail()).toLowerCase(Locale.ROOT);
        String combined = subject + " " + body;

        for (String pattern : ignoreSubjectPatterns) {
            if (subject.contains(pattern.toLowerCase(Locale.ROOT))) {
                log.debug("Message {} ignored by subject pattern: {}", message.getGmailMessageId(), pattern);
                return false;
            }
        }

        for (String allowed : allowedSenders) {
            if (senderEmail.contains(allowed.toLowerCase(Locale.ROOT))) {
                log.debug("Message {} passed by allowedSender: {}", message.getGmailMessageId(), allowed);
                return true;
            }
        }

        for (String keyword : keywords) {
            if (combined.contains(keyword.toLowerCase(Locale.ROOT))) {
                log.debug("Message {} passed by keyword: {}", message.getGmailMessageId(), keyword);
                return true;
            }
        }

        log.debug("Message {} filtered out by rule engine", message.getGmailMessageId());
        return false;
    }

    private String blankToEmpty(String s) {
        return s == null ? "" : s;
    }
}
