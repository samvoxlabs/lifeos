package com.familyos.familyos.authentication.service;

import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.exception.UnauthorizedException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TokenRefreshService {

    private static final String GOOGLE_PROVIDER = "google";

    private final OAuthTokenService oauthTokenService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final RestClient restClient;

    public TokenRefreshService(OAuthTokenService oauthTokenService,
                               ClientRegistrationRepository clientRegistrationRepository,
                               RestClient.Builder restClientBuilder) {
        this.oauthTokenService = oauthTokenService;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.restClient = restClientBuilder.build();
    }

    @Transactional
    public OAuthToken refreshIfNeeded(OAuthToken token) {
        if (token == null) {
            throw new UnauthorizedException("Google token not found");
        }

        if (!token.isExpired() && token.getAccessToken() != null && !token.getAccessToken().isBlank()) {
            return token;
        }

        if (token.getRefreshToken() == null || token.getRefreshToken().isBlank()) {
            throw new UnauthorizedException("Google refresh token not available");
        }

        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(token.getAccount().getProvider());
        if (registration == null || !GOOGLE_PROVIDER.equals(registration.getRegistrationId())) {
            throw new UnauthorizedException("Google client registration not configured");
        }

        RefreshTokenResponse response = restClient.post()
                .uri(registration.getProviderDetails().getTokenUri())
                .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                .body(refreshRequest(registration, token.getRefreshToken()))
                .retrieve()
                .body(RefreshTokenResponse.class);

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new UnauthorizedException("Google token refresh failed");
        }

        Set<String> scopes = response.scope() != null && !response.scope().isBlank()
                ? Arrays.stream(response.scope().split("\\s+"))
                .filter(scope -> !scope.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                : token.scopeSet();

        LocalDateTime expiresAt = response.expiresIn() != null
                ? LocalDateTime.ofInstant(Instant.now().plusSeconds(response.expiresIn()), ZoneId.systemDefault())
                : token.getExpiresAt();

        return oauthTokenService.saveToken(
                token.getAccount(),
                response.accessToken(),
                response.refreshToken() != null && !response.refreshToken().isBlank() ? response.refreshToken() : token.getRefreshToken(),
                response.tokenType() != null && !response.tokenType().isBlank() ? response.tokenType() : token.getTokenType(),
                scopes,
                expiresAt
        );
    }

    public String getValidAccessToken(OAuthToken token) {
        return refreshIfNeeded(token).getAccessToken();
    }

    private MultiValueMap<String, String> refreshRequest(ClientRegistration registration, String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", registration.getClientId());
        form.add("client_secret", registration.getClientSecret());
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        return form;
    }

    private record RefreshTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("scope") String scope,
            @JsonProperty("token_type") String tokenType
    ) {}
}
