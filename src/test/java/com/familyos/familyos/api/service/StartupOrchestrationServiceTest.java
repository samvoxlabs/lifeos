package com.familyos.familyos.api.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.OAuthTokenService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.config.properties.GeminiProperties;
import com.familyos.familyos.config.properties.GroqProperties;
import com.familyos.familyos.config.properties.LlmProperties;
import com.familyos.familyos.config.properties.OpenRouterProperties;
import com.familyos.familyos.domain.service.SourceDocumentSeedImporter;
import com.familyos.familyos.dto.AuthenticatedUser;
import com.familyos.familyos.service.LlmService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartupOrchestrationServiceTest {

    @Mock private AuthenticationService authenticationService;
    @Mock private UserService userService;
    @Mock private OAuthAccountService oauthAccountService;
    @Mock private OAuthTokenService oauthTokenService;
    @Mock private LlmService llmService;
    @Mock private SyncOrchestrationService syncOrchestrationService;
    @Mock private SourceDocumentSeedImporter sourceDocumentSeedImporter;

    @Test
    void startReturnsReadyWhenGoogleAndLlmAreConnected() {
        UUID userId = UUID.randomUUID();
        User user = new User("user@example.com", "User", "google");
        user.setId(userId);
        OAuthAccount account = new OAuthAccount(user, "google", "provider-account", "user@example.com", "User");

        StartupOrchestrationService service = new StartupOrchestrationService(
            authenticationService,
            userService,
            oauthAccountService,
            oauthTokenService,
            llmService,
            new LlmProperties("gemini"),
            new GeminiProperties("api-key", "gemini-2.5-flash", "https://example.com"),
            new GroqProperties("", "model", "https://example.com"),
            new OpenRouterProperties("", "model", "https://example.com"),
            syncOrchestrationService,
            sourceDocumentSeedImporter,
            "google-client-id",
            "google-client-secret"
        );

        when(authenticationService.currentUser()).thenReturn(new AuthenticatedUser(userId.toString(), user.getEmail(), user.getName(), "google"));
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(oauthAccountService.findByUserAndProvider(user, "google")).thenReturn(Optional.of(account));
        when(oauthTokenService.findByAccount(account)).thenReturn(Optional.of(new OAuthToken()));
        when(llmService.checkHealth()).thenReturn(new LlmService.ProviderHealth("gemini", true, "Provider is healthy and responding", java.util.Map.of()));

        var response = service.start();

        assertEquals("READY", response.status());
        assertEquals(true, response.google().connected());
        assertEquals(true, response.llm().connected());
    }
}
