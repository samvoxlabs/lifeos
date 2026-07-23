package com.familyos.familyos.mail.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.exception.UnauthorizedException;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.OAuthTokenService;
import com.familyos.familyos.authentication.service.TokenRefreshService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.mail.exception.MailApiException;
import com.familyos.familyos.mail.model.GoogleAccountContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class GoogleAccountContextResolver {

    private static final String GOOGLE_PROVIDER = "google";
    private static final String GMAIL_READ_SCOPE = "https://www.googleapis.com/auth/gmail.readonly";

    private final UserService userService;
    private final OAuthAccountService oauthAccountService;
    private final OAuthTokenService oauthTokenService;
    private final TokenRefreshService tokenRefreshService;

    public GoogleAccountContextResolver(
            UserService userService,
            OAuthAccountService oauthAccountService,
            OAuthTokenService oauthTokenService,
            TokenRefreshService tokenRefreshService
    ) {
        this.userService = userService;
        this.oauthAccountService = oauthAccountService;
        this.oauthTokenService = oauthTokenService;
        this.tokenRefreshService = tokenRefreshService;
    }

    public GoogleAccountContext resolve(String userId) {
        User user = parseUser(userId);
        OAuthAccount account = oauthAccountService.findByUserAndProvider(user, GOOGLE_PROVIDER)
                .orElseThrow(() -> new MailApiException(HttpStatus.UNAUTHORIZED, "GMAIL_NOT_CONNECTED", "Google account not connected", false));
        OAuthToken token = oauthTokenService.findByAccount(account)
                .orElseThrow(() -> new MailApiException(HttpStatus.UNAUTHORIZED, "GMAIL_AUTH_EXPIRED", "Google token not available", false));
        Set<String> scopes = token.scopeSet();
        if (!scopes.contains(GMAIL_READ_SCOPE)) {
            throw new MailApiException(
                    HttpStatus.FORBIDDEN,
                    "GMAIL_NOT_CONNECTED",
                    "Missing required Gmail scope: https://www.googleapis.com/auth/gmail.readonly",
                    false
            );
        }
        try {
            return new GoogleAccountContext(account, tokenRefreshService.getValidAccessToken(token));
        } catch (UnauthorizedException ex) {
            throw new MailApiException(HttpStatus.UNAUTHORIZED, "GMAIL_AUTH_EXPIRED", "The Gmail connection has expired.", false);
        }
    }

    private User parseUser(String userId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            throw new MailApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATED_USER_INVALID", "Authenticated user not found", false);
        }
        return userService.findById(uuid)
                .orElseThrow(() -> new MailApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATED_USER_INVALID", "Authenticated user not found", false));
    }
}
