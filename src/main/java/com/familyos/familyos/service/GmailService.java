package com.familyos.familyos.service;

import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.repository.OAuthTokenRepository;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.dto.GmailMessageDto;
import com.familyos.familyos.integrations.google.gmail.GoogleGmailClient;
import com.familyos.familyos.integrations.google.gmail.GoogleGmailMessage;
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
    private final OAuthTokenRepository oauthTokenRepository;
    private final GoogleGmailClient googleGmailClient;

    public GmailService(UserService userService, OAuthTokenRepository oauthTokenRepository,
                       GoogleGmailClient googleGmailClient) {
        this.userService = userService;
        this.oauthTokenRepository = oauthTokenRepository;
        this.googleGmailClient = googleGmailClient;
    }

    public List<GmailMessageDto> readLatestMessages(String userEmail) {
        log.debug("Reading latest Gmail messages for user: {}", userEmail);

        // Lookup authenticated user
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        // Lookup OAuthToken for Google provider
        var oauthToken = oauthTokenRepository.findByUserAndProvider(user, GOOGLE_PROVIDER)
                .orElseThrow(() -> new IllegalArgumentException("No Google OAuth token found for user: " + userEmail));

        log.debug("Found Google OAuth token for user: {}", userEmail);

        // Get access token
        String accessToken = oauthToken.getAccessToken();

        // Call Gmail API via integration layer
        log.debug("Calling Gmail API for user: {}", userEmail);
        List<GoogleGmailMessage> googleMessages = googleGmailClient.fetchMessages(accessToken, DEFAULT_MAX_RESULTS);

        // Convert to DTOs (provider-agnostic)
        log.debug("Converting {} Gmail messages to DTOs", googleMessages.size());
        return googleMessages.stream()
                .map(this::convertToDto)
                .toList();
    }

    private GmailMessageDto convertToDto(GoogleGmailMessage googleMessage) {
        return new GmailMessageDto(
                googleMessage.id(),
                googleMessage.threadId(),
                googleMessage.from(),
                googleMessage.subject(),
                googleMessage.date(),
                googleMessage.snippet()
        );
    }
}
