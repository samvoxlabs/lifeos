package com.familyos.familyos.authentication.oauth;

import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.service.JwtService;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.OAuthTokenService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.dto.AuthenticatedUser;
import com.familyos.familyos.dto.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuthSuccessHandler.class);

    private final UserService userService;
    private final OAuthAccountService oauthAccountService;
    private final OAuthTokenService oauthTokenService;
    private final JwtService jwtService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final ObjectMapper objectMapper;

    public OAuthSuccessHandler(UserService userService, OAuthAccountService oauthAccountService,
                               OAuthTokenService oauthTokenService, JwtService jwtService,
                               OAuth2AuthorizedClientService authorizedClientService) {
        this.userService = userService;
        this.oauthAccountService = oauthAccountService;
        this.oauthTokenService = oauthTokenService;
        this.jwtService = jwtService;
        this.authorizedClientService = authorizedClientService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        try {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String provider = oauth2Token.getAuthorizedClientRegistrationId();
            String principalName = oauth2Token.getName();

            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String providerAccountId = oauth2User.getAttribute("sub");
            if (providerAccountId == null || providerAccountId.isBlank()) {
                providerAccountId = principalName;
            }

            log.info("OAuth2 login successful for: {} (provider: {})", email, provider);

            // Find or create user
            User user = userService.findOrCreateUser(email, name, provider);
            var oauthAccount = oauthAccountService.findOrCreateAccount(user, provider, providerAccountId, email, name);

            // Extract and save OAuth tokens
            OAuth2AuthorizedClient authorizedClient = authorizedClientService
                    .loadAuthorizedClient(provider, principalName);

            if (authorizedClient != null) {
                String accessToken = authorizedClient.getAccessToken().getTokenValue();
                String refreshToken = authorizedClient.getRefreshToken() != null
                        ? authorizedClient.getRefreshToken().getTokenValue()
                        : null;
                LocalDateTime expiresAt = authorizedClient.getAccessToken().getExpiresAt() != null
                        ? LocalDateTime.ofInstant(authorizedClient.getAccessToken().getExpiresAt(), java.time.ZoneId.systemDefault())
                        : null;

                log.debug("Saving OAuth tokens for user: {}", email);
                oauthTokenService.saveToken(
                        oauthAccount,
                        accessToken,
                        refreshToken,
                        authorizedClient.getAccessToken().getTokenType().getValue(),
                        authorizedClient.getAccessToken().getScopes(),
                        expiresAt
                );
            } else {
                log.warn("No authorized client found for user: {}, provider: {}", email, provider);
            }

            // Generate LifeOS JWT
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    user.getId().toString(),
                    user.getEmail(),
                    user.getName(),
                    user.getProvider()
            );

            String jwtToken = jwtService.generateToken(authenticatedUser);
            log.info("Generated JWT token for user: {}", email);

            // Return JWT to client
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            LoginResponse loginResponse = new LoginResponse(
                    jwtToken,
                    user.getId().toString(),
                    user.getEmail(),
                    user.getName()
            );
            
            response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
            response.getWriter().flush();

            log.debug("OAuth2 authentication success handler completed for user: {}", email);

        } catch (Exception e) {
            log.error("Error in OAuth2 success handler", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"Authentication failed\"}");
            response.getWriter().flush();
        }
    }
}
