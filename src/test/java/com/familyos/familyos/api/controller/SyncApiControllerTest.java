package com.familyos.familyos.api.controller;

import com.familyos.familyos.api.dto.SyncSummaryResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SyncApiController.class, GoogleSyncController.class})
class SyncApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private SyncOrchestrationService syncOrchestrationService;
    @MockBean private OAuth2AuthorizedClientService authorizedClientService;
    @MockBean private ClientRegistrationRepository clientRegistrationRepository;

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
