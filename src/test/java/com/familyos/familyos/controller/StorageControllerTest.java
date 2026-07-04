package com.familyos.familyos.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.config.TestSecurityConfig;
import com.familyos.familyos.dto.AuthenticatedUser;
import com.familyos.familyos.storage.StorageService;
import com.familyos.familyos.storage.StorageStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class StorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @MockBean
    private StorageService storageService;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    @WithMockUser
    void statusReturnsStorageMetadata() throws Exception {
        when(authenticationService.currentUser()).thenReturn(new AuthenticatedUser("user-id", "user@example.com", "Test User", "google"));
        when(storageService.status("user-id")).thenReturn(new StorageStatus(
                new AuthenticatedUser("user-id", "user@example.com", "Test User", "google"),
                true,
                1,
                java.util.List.of("profile"),
                "2026-07-03T21:00:00Z",
                "2026-07-03T21:30:00Z",
                false
        ));

        mockMvc.perform(get("/storage/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schemaVersion").value(1));
    }

    @Test
    @WithMockUser
    void bootstrapCallsStorageService() throws Exception {
        when(authenticationService.currentUser()).thenReturn(new AuthenticatedUser("user-id", "user@example.com", "Test User", "google"));
        when(storageService.bootstrap("user-id")).thenReturn(new StorageStatus(
                new AuthenticatedUser("user-id", "user@example.com", "Test User", "google"),
                true,
                1,
                java.util.List.of("profile"),
                "2026-07-03T21:00:00Z",
                "2026-07-03T21:00:00Z",
                false
        ));

        mockMvc.perform(post("/storage/bootstrap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dirty").value(false));
    }
}
