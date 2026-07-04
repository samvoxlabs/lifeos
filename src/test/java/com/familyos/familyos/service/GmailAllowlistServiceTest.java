package com.familyos.familyos.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.repository.GmailAllowlistRepository;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.dto.GmailAllowlistDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GmailAllowlistServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private OAuthAccountService oauthAccountService;

    @Mock
    private GmailAllowlistRepository gmailAllowlistRepository;

    private GmailAllowlistService gmailAllowlistService;

    @BeforeEach
    void setUp() {
        gmailAllowlistService = new GmailAllowlistService(userService, oauthAccountService, gmailAllowlistRepository);
    }

    @Test
    void readAllowlistReturnsStoredEntries() {
        User user = user("user@example.com");
        OAuthAccount account = new OAuthAccount(user, "google", "sub-1", "user@example.com", "User");

        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(oauthAccountService.findByUserAndProvider(user, "google")).thenReturn(Optional.of(account));
        when(gmailAllowlistRepository.findByAccountOrderByEntryTypeAscEntryValueAsc(account)).thenReturn(List.of());

        GmailAllowlistDto result = gmailAllowlistService.readAllowlist(user.getId().toString());

        assertEquals(List.of(), result.senders());
        assertEquals(List.of(), result.subjects());
    }

    @Test
    void replaceAllowlistPersistsSendersAndSubjects() {
        User user = user("user@example.com");
        OAuthAccount account = new OAuthAccount(user, "google", "sub-1", "user@example.com", "User");

        gmailAllowlistService.replaceAllowlist(account, List.of("allowed@example.com"), List.of("Invoice"));

        verify(gmailAllowlistRepository).deleteByAccount(account);
    }

    private User user(String email) {
        User user = new User(email, "Test User", "google");
        user.setId(UUID.randomUUID());
        return user;
    }
}
