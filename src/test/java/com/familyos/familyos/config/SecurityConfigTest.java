package com.familyos.familyos.config;

import com.familyos.familyos.authentication.service.JwtService;
import com.familyos.familyos.dto.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @Test
    void apiRequestsRequireJwtAuthentication() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/oauth2/authorization/google")));
    }

    @Test
    void apiRequestsAcceptValidJwtAuthentication() throws Exception {
        String token = jwtService.generateToken(new AuthenticatedUser(
                "user-1",
                "user@example.com",
                "Test User",
                "google"
        ));

        mockMvc.perform(get("/api/test").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to Family OS! This is a test endpoint."));
    }

    @Test
    void oauthAuthorizationEndpointRemainsPublic() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("accounts.google.com")));
    }
}
