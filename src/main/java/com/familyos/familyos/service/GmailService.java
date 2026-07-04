package com.familyos.familyos.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.exception.UnauthorizedException;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.OAuthTokenService;
import com.familyos.familyos.authentication.service.TokenRefreshService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.dto.GmailMessageDto;
import com.familyos.familyos.integrations.google.gmail.GoogleGmailClient;
import com.familyos.familyos.integrations.google.gmail.GoogleGmailMessage;
import com.familyos.familyos.service.email.EmailRuleEngine;
import com.familyos.familyos.service.email.NormalizedEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GmailService {

    private static final Logger log = LoggerFactory.getLogger(GmailService.class);
    private static final String GOOGLE_PROVIDER = "google";
    private static final int DEFAULT_MAX_RESULTS = 10;

    private final UserService userService;
    private final OAuthAccountService oauthAccountService;
    private final OAuthTokenService oauthTokenService;
    private final TokenRefreshService tokenRefreshService;
    private final GoogleGmailClient googleGmailClient;
    private final EmailRuleEngine emailRuleEngine;

    public GmailService(UserService userService, OAuthAccountService oauthAccountService, OAuthTokenService oauthTokenService,
                       TokenRefreshService tokenRefreshService, GoogleGmailClient googleGmailClient,
                       EmailRuleEngine emailRuleEngine) {
        this.userService = userService;
        this.oauthAccountService = oauthAccountService;
        this.oauthTokenService = oauthTokenService;
        this.tokenRefreshService = tokenRefreshService;
        this.googleGmailClient = googleGmailClient;
        this.emailRuleEngine = emailRuleEngine;
    }

    public List<GmailMessageDto> readLatestMessages(String userId) {
        log.debug("Reading latest Gmail messages for user: {}", userId);

        // Lookup authenticated user
        User user;
        try {
            user = userService.findById(java.util.UUID.fromString(userId))
                    .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Authenticated user not found");
        }

        // Lookup OAuth account and token for Google provider
        OAuthAccount oauthAccount = oauthAccountService.findByUserAndProvider(user, GOOGLE_PROVIDER)
                .orElseThrow(() -> new UnauthorizedException("Google account not connected"));

        OAuthToken oauthToken = oauthTokenService.findByAccount(oauthAccount)
                .orElseThrow(() -> new UnauthorizedException("Google token not available"));
        String accessToken = tokenRefreshService.getValidAccessToken(oauthToken);

        // Call Gmail API via integration layer
        log.debug("Calling Gmail API for user: {}", user.getEmail());
        List<GoogleGmailMessage> googleMessages = googleGmailClient.fetchMessages(accessToken, DEFAULT_MAX_RESULTS);

        // Convert to DTOs (provider-agnostic)
        log.debug("Converting {} Gmail messages to DTOs", googleMessages.size());
        return googleMessages.stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<GmailMessageDto> readAllowedMessages(String userId) {
        log.debug("Reading allowed Gmail messages for user: {}", userId);

        User user;
        try {
            user = userService.findById(java.util.UUID.fromString(userId))
                    .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Authenticated user not found");
        }

        OAuthAccount oauthAccount = oauthAccountService.findByUserAndProvider(user, GOOGLE_PROVIDER)
                .orElseThrow(() -> new UnauthorizedException("Google account not connected"));

        OAuthToken oauthToken = oauthTokenService.findByAccount(oauthAccount)
                .orElseThrow(() -> new UnauthorizedException("Google token not available"));
        String accessToken = tokenRefreshService.getValidAccessToken(oauthToken);

        log.debug("Calling Gmail API for user: {}", user.getEmail());
        List<GoogleGmailMessage> googleMessages = googleGmailClient.fetchMessages(accessToken, DEFAULT_MAX_RESULTS);
        List<NormalizedEmail> normalizedEmails = googleMessages.stream().map(this::normalize).toList();
        List<NormalizedEmail> relevantEmails = emailRuleEngine.filterRelevantEmails(oauthAccount, normalizedEmails);

        return relevantEmails.stream()
                .map(this::convertToDto)
                .toList();
    }

    private GmailMessageDto convertToDto(GoogleGmailMessage googleMessage) {
        return convertToDto(normalize(googleMessage));
    }

    private GmailMessageDto convertToDto(NormalizedEmail email) {
        return new GmailMessageDto(
                email.id(),
                email.threadId(),
                email.from(),
                email.subject(),
                email.date(),
                email.snippet()
        );
    }

    private NormalizedEmail normalize(GoogleGmailMessage googleMessage) {
        return new NormalizedEmail(
                googleMessage.id(),
                googleMessage.threadId(),
                googleMessage.from(),
                googleMessage.subject(),
                googleMessage.date(),
                googleMessage.snippet()
        );
    }
}
