package com.familyos.familyos.usecases.emailextraction;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.config.TestSecurityConfig;
import com.familyos.familyos.dto.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class EmailExtractionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

  @MockBean
  private AuthenticationService authenticationService;

  @MockBean
  private EmailExtractionService emailExtractionService;

  @Test
  @WithMockUser
  void extractReturnsStructuredResponse() throws Exception {
    when(authenticationService.currentUser()).thenReturn(new AuthenticatedUser("user-id", "user@example.com", "Test User", "google"));
  when(emailExtractionService.extract("user-id", new EmailExtractionRequest(null))).thenReturn(new EmailExtractionResponse(List.of(
      new EmailExtractionResponse.ExtractedEmail(
        "Homework update",
        "school@example.com",
        "Review the assignment",
        "SCHOOL",
        "HIGH",
        List.of(new EmailExtractionResponse.ActionItem("Review assignment", "2026-07-10")),
        List.of(new EmailExtractionResponse.EventItem("Parent meeting", "2026-07-12")),
        List.of("Teacher"),
        List.of("Reply by Friday")
      )
    )));

    mockMvc.perform(post("/llm/email/extract").content("{}").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.emails[0].subject").value("Homework update"))
      .andExpect(jsonPath("$.emails[0].category").value("SCHOOL"))
      .andExpect(jsonPath("$.emails[0].actionItems[0].title").value("Review assignment"));
  }
}
