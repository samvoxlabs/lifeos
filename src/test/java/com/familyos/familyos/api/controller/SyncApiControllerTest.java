package com.familyos.familyos.api.controller;

import com.familyos.familyos.api.dto.ConnectionStatusResponse;
import com.familyos.familyos.api.dto.PopulateResponse;
import com.familyos.familyos.api.dto.StartResponse;
import com.familyos.familyos.api.dto.SyncSummaryResponse;
import com.familyos.familyos.api.service.StartupOrchestrationService;
import com.familyos.familyos.api.service.SyncOrchestrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SyncApiController.class, GoogleSyncController.class})
class SyncApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private SyncOrchestrationService syncOrchestrationService;
    @MockBean private StartupOrchestrationService startupOrchestrationService;
    @MockBean private OAuth2AuthorizedClientService authorizedClientService;
    @MockBean private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    @WithMockUser
    void supportsStartEndpoint() throws Exception {
        when(startupOrchestrationService.start()).thenReturn(new StartResponse(
            "READY",
            new ConnectionStatusResponse(true, true, "Google account is connected."),
            new ConnectionStatusResponse(true, true, "Provider is healthy and responding")
        ));

        mockMvc.perform(get("/api/start"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("READY"))
            .andExpect(jsonPath("$.google.connected").value(true))
            .andExpect(jsonPath("$.llm.connected").value(true));
    }

    @Test
    @WithMockUser
    void supportsPopulateEndpoint() throws Exception {
        when(startupOrchestrationService.populate(true)).thenReturn(new PopulateResponse(
            "SEED",
            "COMPLETED",
            4,
            new SyncSummaryResponse("COMPLETED", 0, 0, 0, 4, 2, 1, 1, 125),
            "Seed data imported and processed."
        ));

        mockMvc.perform(get("/api/populate").param("useSeedData", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mode").value("SEED"))
            .andExpect(jsonPath("$.seedDocumentsImported").value(4))
            .andExpect(jsonPath("$.summary.documentsProcessed").value(4));
    }

    @Test
    @WithMockUser
    void returnsSyncSummary() throws Exception {
        when(syncOrchestrationService.syncAll()).thenReturn(new SyncSummaryResponse(
            "COMPLETED", 42, 6, 36, 5, 4, 1, 2, 2150
        ));

        mockMvc.perform(post("/api/sync").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.documentsRead").value(42))
            .andExpect(jsonPath("$.tasksCreated").value(4));
    }

    @Test
    @WithMockUser
    void supportsInternalGoogleSyncEndpoints() throws Exception {
        when(syncOrchestrationService.syncGmail()).thenReturn(new SyncSummaryResponse(
            "COMPLETED", 10, 2, 8, 2, 1, 1, 0, 500
        ));

        mockMvc.perform(post("/api/google/sync/gmail").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.documentsRead").value(10));
    }
}
