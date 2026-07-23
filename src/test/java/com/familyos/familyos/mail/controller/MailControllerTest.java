package com.familyos.familyos.mail.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.config.TestSecurityConfig;
import com.familyos.familyos.dto.AuthenticatedUser;
import com.familyos.familyos.mail.api.dto.ReviewScheduleResponse;
import com.familyos.familyos.mail.service.MailboxService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class MailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private MailboxService mailboxService;

    @Test
    @WithMockUser
    void reviewUsesAuthenticatedUserId() throws Exception {
        when(authenticationService.currentUser()).thenReturn(new AuthenticatedUser("user-id", "user@example.com", "Test User", "google"));
        when(mailboxService.reviewSchedule("user-id", null)).thenReturn(new ReviewScheduleResponse("mailbox", "2026-07-23T03:15:34-05:00", 0, 0, java.util.List.of(), java.util.List.of()));

        mockMvc.perform(post("/api/mail/review").contentType("application/json").content("{}"))
                .andExpect(status().isOk());
    }
}
