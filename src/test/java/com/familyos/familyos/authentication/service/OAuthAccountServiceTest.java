package com.familyos.familyos.authentication.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.repository.OAuthAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthAccountServiceTest {

    @Mock
    private OAuthAccountRepository oauthAccountRepository;

    private OAuthAccountService oauthAccountService;

    @BeforeEach
    void setUp() {
        oauthAccountService = new OAuthAccountService(oauthAccountRepository);
    }

    @Test
    void findOrCreateAccountCreatesNewRecord() {
        User user = user("user@example.com");
        when(oauthAccountRepository.findByUserAndProvider(user, "google")).thenReturn(Optional.empty());
        when(oauthAccountRepository.save(any(OAuthAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuthAccount account = oauthAccountService.findOrCreateAccount(user, "google", "sub-1", "user@example.com", "Test User");

        assertEquals("google", account.getProvider());
        assertEquals("sub-1", account.getProviderAccountId());
        verify(oauthAccountRepository).save(any(OAuthAccount.class));
    }

    @Test
    void findByUserAndProviderDelegatesToRepository() {
        User user = user("user@example.com");
        when(oauthAccountRepository.findByUserAndProvider(user, "google")).thenReturn(Optional.empty());

        oauthAccountService.findByUserAndProvider(user, "google");

        verify(oauthAccountRepository).findByUserAndProvider(user, "google");
    }

    private User user(String email) {
        User user = new User(email, "Test User", "google");
        user.setId(UUID.randomUUID());
        return user;
    }
}
