package com.familyos.familyos.config;

import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.service.JwtService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.dto.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

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

    @MockBean
    private UserService userService;

    @Test
    void apiRequestsRequireJwtAuthentication() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiRequestsAcceptValidJwtAuthentication() throws Exception {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(userService.findById(userId)).thenReturn(Optional.of(user(userId)));
        String token = jwtService.generateToken(new AuthenticatedUser(
                userId.toString(),
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

    @Test
    void expiredJwtIsRejected() throws Exception {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(userService.findById(userId)).thenReturn(Optional.of(user(userId)));
        JwtService shortLivedJwtService = new JwtService(new com.familyos.familyos.config.properties.JwtProperties(
                "test-secret-key-that-is-long-enough-for-hs256-algorithm",
                100L
        ));
        String token = shortLivedJwtService.generateToken(new AuthenticatedUser(
                userId.toString(),
                "user@example.com",
                "Test User",
                "google"
        ));
        Thread.sleep(150);

        mockMvc.perform(get("/api/test").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private User user(UUID id) {
        User user = new User("user@example.com", "Test User", "google");
        user.setId(id);
        return user;
    }
}
