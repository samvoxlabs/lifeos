package com.familyos.familyos.authentication.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.repository.OAuthAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OAuthAccountService {

    private static final Logger log = LoggerFactory.getLogger(OAuthAccountService.class);

    private final OAuthAccountRepository oauthAccountRepository;

    public OAuthAccountService(OAuthAccountRepository oauthAccountRepository) {
        this.oauthAccountRepository = oauthAccountRepository;
    }

    @Transactional
    public OAuthAccount findOrCreateAccount(User user, String provider, String providerAccountId, String email, String displayName) {
        Optional<OAuthAccount> existingAccount = oauthAccountRepository.findByUserAndProvider(user, provider);

        if (existingAccount.isPresent()) {
            OAuthAccount account = existingAccount.get();
            account.setProviderAccountId(providerAccountId);
            account.setEmail(email);
            account.setDisplayName(displayName);
            return oauthAccountRepository.save(account);
        }

        OAuthAccount account = new OAuthAccount(user, provider, providerAccountId, email, displayName);
        log.debug("Creating OAuth account for user: {}, provider: {}", user.getEmail(), provider);
        return oauthAccountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Optional<OAuthAccount> findByUserAndProvider(User user, String provider) {
        return oauthAccountRepository.findByUserAndProvider(user, provider);
    }
}
