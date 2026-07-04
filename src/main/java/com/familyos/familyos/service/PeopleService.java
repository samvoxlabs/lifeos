package com.familyos.familyos.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.exception.UnauthorizedException;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.OAuthTokenService;
import com.familyos.familyos.authentication.service.TokenRefreshService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.config.properties.GoogleProperties;
import com.familyos.familyos.dto.ContactDto;
import com.familyos.familyos.integrations.google.people.GoogleContact;
import com.familyos.familyos.integrations.google.people.GooglePeopleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PeopleService {

    private static final Logger log = LoggerFactory.getLogger(PeopleService.class);
    private static final String GOOGLE_PROVIDER = "google";
    private static final int DEFAULT_MAX_RESULTS = 20;

    private final UserService userService;
    private final OAuthAccountService oauthAccountService;
    private final OAuthTokenService oauthTokenService;
    private final TokenRefreshService tokenRefreshService;
    private final GooglePeopleClient googlePeopleClient;
    private final GoogleProperties googleProperties;

    public PeopleService(UserService userService,
                         OAuthAccountService oauthAccountService,
                         OAuthTokenService oauthTokenService,
                         TokenRefreshService tokenRefreshService,
                         GooglePeopleClient googlePeopleClient,
                         GoogleProperties googleProperties) {
        this.userService = userService;
        this.oauthAccountService = oauthAccountService;
        this.oauthTokenService = oauthTokenService;
        this.tokenRefreshService = tokenRefreshService;
        this.googlePeopleClient = googlePeopleClient;
        this.googleProperties = googleProperties;
    }

    public List<ContactDto> readContacts(String userId) {
        log.debug("Reading contacts for user: {}", userId);

        User user;
        try {
            user = userService.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Authenticated user not found");
        }

        OAuthAccount oauthAccount = oauthAccountService.findByUserAndProvider(user, GOOGLE_PROVIDER)
                .orElseThrow(() -> new UnauthorizedException("Google account not connected"));

        OAuthToken oauthToken = oauthTokenService.findByAccount(oauthAccount)
                .orElseThrow(() -> new UnauthorizedException("Google token not available"));
        String accessToken = tokenRefreshService.getValidAccessToken(oauthToken);

        int maxResults = googleProperties.people() != null && googleProperties.people().maxResults() != null
                ? googleProperties.people().maxResults()
                : DEFAULT_MAX_RESULTS;

        log.debug("Calling Google People API for user: {}", user.getEmail());
        List<GoogleContact> contacts = googlePeopleClient.fetchContacts(accessToken, maxResults);

        return contacts.stream()
                .map(this::toDto)
                .toList();
    }

    private ContactDto toDto(GoogleContact contact) {
        return new ContactDto(
                contact.resourceName(),
                contact.displayName(),
                contact.email(),
                contact.phoneNumber()
        );
    }
}
