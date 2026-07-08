package com.familyos.familyos.api.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.domain.repository.ActionRepository;
import com.familyos.familyos.domain.repository.ExtractionRepository;
import com.familyos.familyos.domain.repository.SourceDocumentRepository;
import com.familyos.familyos.dto.AuthenticatedUser;
import com.familyos.familyos.service.CalendarService;
import com.familyos.familyos.service.DriveService;
import com.familyos.familyos.service.GmailService;
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

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FrontendApiIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ActionRepository actionRepository;
    @Autowired private ExtractionRepository extractionRepository;
    @Autowired private SourceDocumentRepository sourceDocumentRepository;

    @MockBean private OAuth2AuthorizedClientService authorizedClientService;
    @MockBean private ClientRegistrationRepository clientRegistrationRepository;
    @MockBean private GmailService gmailService;
    @MockBean private CalendarService calendarService;
    @MockBean private DriveService driveService;
    @MockBean private AuthenticationService authenticationService;

    @BeforeEach
    void setup() {
        actionRepository.deleteAll();
        extractionRepository.deleteAll();
        sourceDocumentRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void exposesDashboardTimelineSearchAndSourceDocuments() throws Exception {
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
                  {"type":"EVENT","title":"Parent teacher meeting","description":"At school"},
                  {"type":"REMINDER","title":"Bring documents","description":"Bring report card"}
                ]
              }
            }
            """;

        mockMvc.perform(post("/api/domain/process")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.summary.pendingTasks").value(1))
            .andExpect(jsonPath("$.summary.upcomingEvents").value(1))
            .andExpect(jsonPath("$.summary.activeReminders").value(1));

        mockMvc.perform(get("/api/tasks?page=0&size=10&status=OPEN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/events?page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/reminders?page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/timeline"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/api/search?q=attendance"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tasks.length()").value(1));

        mockMvc.perform(get("/api/source-documents?page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser
    void exposesSyncOrchestrationEndpoint() throws Exception {
        when(authenticationService.currentUser()).thenReturn(new AuthenticatedUser("user-1", "user@example.com", "User", "google"));
        when(gmailService.readLatestMessages("user-1")).thenReturn(List.of());
        when(calendarService.readUpcomingEvents("user-1")).thenReturn(List.of());
        when(driveService.readRecentFiles("user-1")).thenReturn(List.of());

        mockMvc.perform(post("/api/sync").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.documentsRead").value(0))
            .andExpect(jsonPath("$.documentsImported").value(0));
    }
}
