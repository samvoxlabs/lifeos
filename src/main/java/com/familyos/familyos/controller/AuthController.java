package com.familyos.familyos.controller;

import com.familyos.familyos.authentication.exception.UnauthorizedException;
import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.OAuthTokenService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.dto.AuthenticatedUser;
import com.familyos.familyos.dto.GoogleAuthDetailsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class AuthController {

  private static final String GOOGLE_PROVIDER = "google";
  private static final String GOOGLE_REGISTRATION_ID = "google";

  private final AuthenticationService authenticationService;
  private final UserService userService;
  private final OAuthAccountService oauthAccountService;
  private final OAuthTokenService oauthTokenService;
  private final String googleClientId;
  private final String googleProjectId;
  private final String googleProjectName;
  private final String googleRegistrationId;
  private final String googleRedirectUri;

  public AuthController(AuthenticationService authenticationService,
                        UserService userService,
                        OAuthAccountService oauthAccountService,
                        OAuthTokenService oauthTokenService,
                        @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
                        @Value("${google.project-id:}") String googleProjectId,
                        @Value("${google.project-name:}") String googleProjectName,
                        @Value("${spring.security.oauth2.client.registration.google.redirect-uri:}") String googleRedirectUri) {
    this.authenticationService = authenticationService;
    this.userService = userService;
    this.oauthAccountService = oauthAccountService;
    this.oauthTokenService = oauthTokenService;
    this.googleClientId = googleClientId;
    this.googleProjectId = googleProjectId;
    this.googleProjectName = googleProjectName;
    this.googleRegistrationId = GOOGLE_REGISTRATION_ID;
    this.googleRedirectUri = googleRedirectUri;
  }

  @GetMapping("/")
  public String home() {
    return "LifeOS Gmail POC is running";
  }

  @GetMapping("/user")
  public AuthenticatedUser user() {
    return authenticationService.currentUser();
  }

  @GetMapping("/auth/google/scopes")
  public GoogleAuthDetailsResponse googleAuthDetails() {
    AuthenticatedUser currentUser = authenticationService.currentUser();
    User user = userService.findById(UUID.fromString(currentUser.id()))
        .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
    OAuthAccount account = oauthAccountService.findByUserAndProvider(user, GOOGLE_PROVIDER)
        .orElseThrow(() -> new UnauthorizedException("Google account not connected"));
    OAuthToken token = oauthTokenService.findByAccount(account)
        .orElseThrow(() -> new UnauthorizedException("Google token not available"));
    List<String> scopes = token.scopeSet().stream().toList();
    return new GoogleAuthDetailsResponse(
        currentUser.id(),
        GOOGLE_PROVIDER,
        googleRegistrationId,
        googleClientId,
        googleProjectId,
        googleProjectName,
        googleRedirectUri,
        scopes
    );
  }
}
