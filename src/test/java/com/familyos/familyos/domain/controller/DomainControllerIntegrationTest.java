package com.familyos.familyos.domain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.familyos.familyos.domain.repository.ActionRepository;
import com.familyos.familyos.domain.repository.ExtractionRepository;
import com.familyos.familyos.domain.repository.SourceDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DomainControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ActionRepository actionRepository;
    @Autowired private ExtractionRepository extractionRepository;
    @Autowired private SourceDocumentRepository sourceDocumentRepository;

    @MockBean private OAuth2AuthorizedClientService authorizedClientService;
    @MockBean private ClientRegistrationRepository clientRegistrationRepository;

    @BeforeEach
    void cleanDomainTables() {
        actionRepository.deleteAll();
        extractionRepository.deleteAll();
        sourceDocumentRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void processAndRetrieveActions() throws Exception {
        String request = """
            {
              "sourceDocument": {
                "id": "doc-1",
                "sender": "school@example.com",
                "subject": "Parent teacher meeting",
                "content": "Meeting next week",
                "labels": ["school"],
                "priority": "1",
                "source": "email",
                "provider": "google",
                "sourceType": "gmail",
                "externalId": "msg-1",
                "rawContent": "Meeting next week",
                "metadata": {
                  "model": "gemini-2.5-flash",
                  "llmProvider": "google",
                  "promptVersion": "v1"
                }
              },
              "extractionResult": {
                "summary": "Meeting and follow up",
                "confidence": 0.95,
                "actions": [
                  {"type":"TASK","title":"Confirm attendance","description":"Reply to teacher"},
                  {"type":"EVENT","title":"Parent teacher meeting","description":"At school","dueDate":"2026-07-10T10:00:00"},
                  {"type":"REMINDER","title":"Bring documents","description":"Bring report card"}
                ]
              }
            }
            """;

        mockMvc.perform(post("/api/domain/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sourceDocument.externalId").value("msg-1"))
            .andExpect(jsonPath("$.actions.length()").value(3));

        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/reminders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void duplicateProcessingDoesNotCreateDuplicateSourceDocumentActions() throws Exception {
        String request = """
            {
              "sourceDocument": {
                "id": "doc-dup",
                "sender": "clinic@example.com",
                "subject": "Appointment",
                "content": "Appointment reminder",
                "labels": [],
                "priority": "1",
                "source": "email",
                "provider": "google",
                "sourceType": "gmail",
                "externalId": "msg-dup",
                "rawContent": "Appointment reminder",
                "metadata": {}
              },
              "extractionResult": {
                "summary": "Appointment reminder",
                "confidence": 0.9,
                "actions": [
                  {"type":"TASK","title":"Confirm appointment","description":"Call clinic"}
                ]
              }
            }
            """;

        mockMvc.perform(post("/api/domain/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.actions.length()").value(1));

        mockMvc.perform(post("/api/domain/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.actions.length()").value(1));
    }

    @Test
    @WithMockUser
    void invalidExtractionReturnsBadRequest() throws Exception {
        String invalidRequest = """
            {
              "sourceDocument": {
                "id": "doc-2",
                "sender": "bank@example.com",
                "subject": "Statement",
                "content": "Monthly statement",
                "labels": [],
                "priority": "1",
                "source": "email",
                "provider": "",
                "sourceType": "gmail",
                "externalId": "msg-2",
                "rawContent": "Monthly statement",
                "metadata": {}
              },
              "extractionResult": {
                "summary": "Monthly statement",
                "confidence": 0.8,
                "actions": []
              }
            }
            """;

        mockMvc.perform(post("/api/domain/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest());
    }
}
