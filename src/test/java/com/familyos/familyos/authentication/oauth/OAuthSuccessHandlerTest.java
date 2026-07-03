package com.familyos.familyos.authentication.oauth;

import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.service.JwtService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.config.properties.JwtProperties;
import com.familyos.familyos.dto.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OAuthSuccessHandlerTest {

    private UserService userService;
    private JwtService jwtService;
    private OAuth2AuthorizedClientService authorizedClientService;
    private OAuthSuccessHandler handler;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        jwtService = mock(JwtService.class);
        authorizedClientService = mock(OAuth2AuthorizedClientService.class);
        handler = new OAuthSuccessHandler(userService, jwtService, authorizedClientService);
    }

    @Test
    void shouldPersistUserTokensAndReturnJwtResponse() throws Exception {
        User user = createUser();
        when(userService.findOrCreateUser("user@example.com", "Test User", "google")).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("lifeos.jwt.token");
        when(authorizedClientService.loadAuthorizedClient(eq("google"), eq("user@example.com")))
                .thenReturn(createAuthorizedClient());

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication());

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));

        LoginResponse loginResponse = new ObjectMapper().readValue(response.getContentAsString(), LoginResponse.class);
        assertEquals("lifeos.jwt.token", loginResponse.token());
        assertEquals(user.getId().toString(), loginResponse.userId());
        assertEquals(user.getEmail(), loginResponse.email());
        assertEquals(user.getName(), loginResponse.name());

        ArgumentCaptor<com.familyos.familyos.dto.AuthenticatedUser> userCaptor =
                ArgumentCaptor.forClass(com.familyos.familyos.dto.AuthenticatedUser.class);
        verify(jwtService).generateToken(userCaptor.capture());
        assertEquals(user.getEmail(), userCaptor.getValue().email());
        verify(userService).saveOAuthTokens(
                eq(user),
                eq("google"),
                eq("access-token"),
                eq("refresh-token"),
                eq("Bearer"),
                any(LocalDateTime.class)
        );
    }

    @Test
    void shouldStillReturnJwtWhenAuthorizedClientIsMissing() throws Exception {
        User user = createUser();
        when(userService.findOrCreateUser("user@example.com", "Test User", "google")).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("lifeos.jwt.token");
        when(authorizedClientService.loadAuthorizedClient("google", "user@example.com")).thenReturn(null);

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication());

        assertEquals(200, response.getStatus());
        verify(userService, never()).saveOAuthTokens(any(), any(), any(), any(), any(), any());
    }

    private OAuth2AuthenticationToken authentication() {
        DefaultOAuth2User oauth2User = new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", "subject-1", "email", "user@example.com", "name", "Test User"),
                "sub"
        );
        return new OAuth2AuthenticationToken(oauth2User, oauth2User.getAuthorities(), "google");
    }

    private OAuth2AuthorizedClient createAuthorizedClient() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("google")
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

        return new OAuth2AuthorizedClient(
                registration,
                "user@example.com",
                new OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER,
                        "access-token",
                        Instant.now(),
                        Instant.now().plusSeconds(3600)
                ),
                new OAuth2RefreshToken("refresh-token", Instant.now())
        );
    }

    private User createUser() {
        User user = new User("user@example.com", "Test User", "google");
        user.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        return user;
    }
}
