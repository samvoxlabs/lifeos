package com.familyos.familyos.authentication.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.repository.OAuthTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class OAuthTokenService {

    private static final Logger log = LoggerFactory.getLogger(OAuthTokenService.class);

    private final OAuthTokenRepository oauthTokenRepository;

    public OAuthTokenService(OAuthTokenRepository oauthTokenRepository) {
        this.oauthTokenRepository = oauthTokenRepository;
    }

    @Transactional
    public OAuthToken saveToken(OAuthAccount account, String accessToken, String refreshToken,
                               String tokenType, Set<String> scopes, LocalDateTime expiresAt) {
        Optional<OAuthToken> existingToken = oauthTokenRepository.findByAccount(account);

        OAuthToken token;
        if (existingToken.isPresent()) {
            token = existingToken.get();
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setTokenType(tokenType);
            token.setScopeSet(scopes);
            token.setExpiresAt(expiresAt);
            log.debug("Updated OAuth token for account: {}", account.getId());
        } else {
            token = new OAuthToken(account, accessToken, refreshToken, tokenType, scopes == null ? "" : String.join(" ", scopes), expiresAt);
            log.debug("Created OAuth token for account: {}", account.getId());
        }

        return oauthTokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public Optional<OAuthToken> findByAccount(OAuthAccount account) {
        return oauthTokenRepository.findByAccount(account);
    }
}
