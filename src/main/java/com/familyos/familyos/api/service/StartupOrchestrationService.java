package com.familyos.familyos.api.service;

import com.familyos.familyos.api.dto.ConnectionStatusResponse;
import com.familyos.familyos.api.dto.PopulateResponse;
import com.familyos.familyos.api.dto.StartResponse;
import com.familyos.familyos.api.dto.SyncSummaryResponse;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class StartupOrchestrationService {

    private static final String GOOGLE_PROVIDER = "google";

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final OAuthAccountService oauthAccountService;
    private final OAuthTokenService oauthTokenService;
    private final LlmService llmService;
    private final LlmProperties llmProperties;
    private final GeminiProperties geminiProperties;
    private final GroqProperties groqProperties;
    private final OpenRouterProperties openRouterProperties;
    private final SyncOrchestrationService syncOrchestrationService;
    private final SourceDocumentSeedImporter sourceDocumentSeedImporter;
    private final String googleClientId;
    private final String googleClientSecret;

    public StartupOrchestrationService(
        AuthenticationService authenticationService,
        UserService userService,
        OAuthAccountService oauthAccountService,
        OAuthTokenService oauthTokenService,
        LlmService llmService,
        LlmProperties llmProperties,
        GeminiProperties geminiProperties,
        GroqProperties groqProperties,
        OpenRouterProperties openRouterProperties,
        SyncOrchestrationService syncOrchestrationService,
        SourceDocumentSeedImporter sourceDocumentSeedImporter,
        @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
        @Value("${spring.security.oauth2.client.registration.google.client-secret:}") String googleClientSecret
    ) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.oauthAccountService = oauthAccountService;
        this.oauthTokenService = oauthTokenService;
        this.llmService = llmService;
        this.llmProperties = llmProperties;
        this.geminiProperties = geminiProperties;
        this.groqProperties = groqProperties;
        this.openRouterProperties = openRouterProperties;
        this.syncOrchestrationService = syncOrchestrationService;
        this.sourceDocumentSeedImporter = sourceDocumentSeedImporter;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
    }

    public StartResponse start() {
        ConnectionStatusResponse googleStatus = googleConnectionStatus();
        ConnectionStatusResponse llmStatus = llmConnectionStatus();
        String status = googleStatus.connected() && llmStatus.connected() ? "READY" : "NOT_READY";
        return new StartResponse(status, googleStatus, llmStatus);
    }

    public PopulateResponse populate(boolean useSeedData) throws Exception {
        if (useSeedData) {
            int seedImported = sourceDocumentSeedImporter.importSeedData(true);
            SyncSummaryResponse summary = syncOrchestrationService.processPendingDocuments();
            String message = seedImported > 0
                ? "Seed data imported and processed."
                : "Seed import skipped because source documents already exist. Processed pending documents only.";
            return new PopulateResponse("SEED", "COMPLETED", seedImported, summary, message);
        }

        SyncSummaryResponse summary = syncOrchestrationService.syncAll();
        return new PopulateResponse("SYNC", "COMPLETED", 0, summary, "Data synced from connected providers.");
    }

    private ConnectionStatusResponse googleConnectionStatus() {
        boolean configured = hasText(googleClientId) && hasText(googleClientSecret);
        if (!configured) {
            return new ConnectionStatusResponse(false, false, "Google OAuth client ID/secret are not configured.");
        }

        AuthenticatedUser authenticatedUser = authenticationService.currentUser();
        UUID userId = UUID.fromString(authenticatedUser.id());
        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) {
            return new ConnectionStatusResponse(true, false, "Authenticated user record not found.");
        }

        boolean connected = oauthAccountService.findByUserAndProvider(user.get(), GOOGLE_PROVIDER)
            .flatMap(oauthTokenService::findByAccount)
            .isPresent();

        if (!connected) {
            return new ConnectionStatusResponse(true, false, "Google account/token not connected for this user.");
        }
        return new ConnectionStatusResponse(true, true, "Google account is connected.");
    }

    private ConnectionStatusResponse llmConnectionStatus() {
        String provider = llmProperties.defaultProvider();
        boolean configured = switch (provider.toLowerCase()) {
            case "gemini" -> hasText(geminiProperties.apiKey());
            case "groq" -> hasText(groqProperties.apiKey());
            case "openrouter" -> hasText(openRouterProperties.apiKey());
            default -> false;
        };

        if (!configured) {
            return new ConnectionStatusResponse(
                false,
                false,
                "LLM provider '" + provider + "' is selected but its API key is missing."
            );
        }

        LlmService.ProviderHealth health = llmService.checkHealth();
        return new ConnectionStatusResponse(true, health.healthy(), health.message());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
