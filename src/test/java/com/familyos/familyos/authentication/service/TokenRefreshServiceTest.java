package com.familyos.familyos.authentication.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;
import static org.hamcrest.Matchers.containsString;

class TokenRefreshServiceTest {

    private OAuthTokenService oauthTokenService;
    private ClientRegistrationRepository clientRegistrationRepository;
    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;
    private TokenRefreshService tokenRefreshService;

    @BeforeEach
    void setUp() {
        oauthTokenService = mock(OAuthTokenService.class);
        clientRegistrationRepository = new InMemoryClientRegistrationRepository(googleRegistration());
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        tokenRefreshService = new TokenRefreshService(oauthTokenService, clientRegistrationRepository, restClientBuilder);
    }

    @Test
    void refreshIfNeededReturnsExistingTokenWhenStillValid() {
        OAuthToken token = token(false);

        OAuthToken result = tokenRefreshService.refreshIfNeeded(token);

        assertSame(token, result);
        verifyNoInteractions(oauthTokenService);
    }

    @Test
    void refreshIfNeededRefreshesExpiredToken() {
        OAuthToken token = token(true);
        OAuthToken refreshed = new OAuthToken(token.getAccount(), "new-access-token", "refresh-token", "Bearer", "openid email", LocalDateTime.now().plusHours(1));

        when(oauthTokenService.saveToken(any(), eq("new-access-token"), eq("refresh-token"), eq("Bearer"), anySet(), any()))
                .thenReturn(refreshed);

        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andExpect(method(POST))
                .andExpect(content().string(containsString("grant_type=refresh_token")))
                .andRespond(withSuccess("""
                        {"access_token":"new-access-token","expires_in":3600,"token_type":"Bearer","scope":"openid email"}
                        """, MediaType.APPLICATION_JSON));

        OAuthToken result = tokenRefreshService.refreshIfNeeded(token);

        assertEquals("new-access-token", result.getAccessToken());
        verify(oauthTokenService).saveToken(any(), eq("new-access-token"), eq("refresh-token"), eq("Bearer"), anySet(), any());
        server.verify();
    }

    private OAuthToken token(boolean expired) {
        User user = new User("user@example.com", "Test User", "google");
        user.setId(UUID.randomUUID());
        OAuthAccount account = new OAuthAccount(user, "google", "subject-1", "user@example.com", "Test User");
        return new OAuthToken(
                account,
                "old-access-token",
                "refresh-token",
                "Bearer",
                "openid email",
                expired ? LocalDateTime.now().minusHours(1) : LocalDateTime.now().plusHours(1)
        );
    }

    private ClientRegistration googleRegistration() {
        return ClientRegistration.withRegistrationId("google")
                .clientId("client-id")
                .clientSecret("client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .build();
    }
}
