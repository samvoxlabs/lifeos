package com.familyos.familyos.authentication.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.repository.OAuthTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthTokenServiceTest {

    @Mock
    private OAuthTokenRepository oauthTokenRepository;

    private OAuthTokenService oauthTokenService;

    @BeforeEach
    void setUp() {
        oauthTokenService = new OAuthTokenService(oauthTokenRepository);
    }

    @Test
    void saveTokenStoresScopesAndAccountReference() {
        OAuthAccount account = account();
        when(oauthTokenRepository.findByAccount(account)).thenReturn(Optional.empty());
        when(oauthTokenRepository.save(any(OAuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuthToken token = oauthTokenService.saveToken(
                account,
                "access-token",
                "refresh-token",
                "Bearer",
                Set.of("openid", "email"),
                LocalDateTime.now().plusHours(1)
        );

        assertEquals("access-token", token.getAccessToken());
        assertEquals(Set.of("openid", "email"), token.scopeSet());
        verify(oauthTokenRepository).save(any(OAuthToken.class));
    }

    @Test
    void findByAccountDelegatesToRepository() {
        OAuthAccount account = account();
        when(oauthTokenRepository.findByAccount(account)).thenReturn(Optional.empty());

        oauthTokenService.findByAccount(account);

        verify(oauthTokenRepository).findByAccount(account);
    }

    private OAuthAccount account() {
        User user = new User("user@example.com", "Test User", "google");
        user.setId(UUID.randomUUID());
        OAuthAccount account = new OAuthAccount(user, "google", "sub-1", "user@example.com", "Test User");
        account.setId(UUID.randomUUID());
        return account;
    }
}
