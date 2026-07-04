package com.familyos.familyos.storage;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.OAuthTokenService;
import com.familyos.familyos.authentication.service.TokenRefreshService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.storage.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {

    @Mock private UserService userService;
    @Mock private OAuthAccountService oauthAccountService;
    @Mock private OAuthTokenService oauthTokenService;
    @Mock private TokenRefreshService tokenRefreshService;
    @Mock private StorageBootstrapService storageBootstrapService;
    @Mock private ExportService exportService;
    @Mock private GoogleDriveStorageService googleDriveStorageService;

    private StorageState storageState;
    private StorageServiceImpl storageService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        storageState = new StorageState();
        objectMapper = new ObjectMapper();
        storageService = new StorageServiceImpl(
                userService,
                oauthAccountService,
                oauthTokenService,
                tokenRefreshService,
                storageBootstrapService,
                exportService,
                googleDriveStorageService,
                storageState,
                objectMapper
        );
    }

    @Test
    void saveMarksStorageAsClean() throws Exception {
        User user = user();
        OAuthAccount account = account(user);
        OAuthToken token = token(account);
        StorageBundleDto bundle = bundle(user);

        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(oauthAccountService.findByUserAndProvider(user, "google")).thenReturn(Optional.of(account));
        when(oauthTokenService.findByAccount(account)).thenReturn(Optional.of(token));
        when(tokenRefreshService.getValidAccessToken(token)).thenReturn("access-token");
        when(exportService.exportBundle(eq(user.getId().toString()), any())).thenReturn(bundle);

        storageService.save(user.getId().toString());

        assertFalse(storageService.status(user.getId().toString()).dirty());
        verify(googleDriveStorageService).writeBundle(eq("access-token"), eq(user.getId().toString()), any());
    }

    @Test
    void bootstrapMarksStorageAsClean() {
        User user = user();
        OAuthAccount account = account(user);
        OAuthToken token = token(account);
        StorageBundleDto bundle = bundle(user);

        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(oauthAccountService.findByUserAndProvider(user, "google")).thenReturn(Optional.of(account));
        when(oauthTokenService.findByAccount(account)).thenReturn(Optional.of(token));
        when(tokenRefreshService.getValidAccessToken(token)).thenReturn("access-token");
        when(storageBootstrapService.bootstrap(user, "access-token")).thenReturn(bundle);
        when(exportService.exportBundle(eq(user.getId().toString()), any())).thenReturn(bundle);

        storageService.bootstrap(user.getId().toString());

        assertFalse(storageService.status(user.getId().toString()).dirty());
    }

    @Test
    void statusIsDirtyBeforeAnySync() {
        User user = user();
        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(exportService.exportBundle(eq(user.getId().toString()), any())).thenReturn(bundle(user));

        assertTrue(storageService.status(user.getId().toString()).dirty());
    }

    private User user() {
        User user = new User("user@example.com", "Test User", "google");
        user.setId(UUID.randomUUID());
        return user;
    }

    private OAuthAccount account(User user) {
        return new OAuthAccount(user, "google", "sub-1", "user@example.com", "Test User");
    }

    private OAuthToken token(OAuthAccount account) {
        return new OAuthToken(account, "access-token", "refresh-token", "Bearer", "openid email", LocalDateTime.now().plusHours(1));
    }

    private StorageBundleDto bundle(User user) {
        return new StorageBundleDto(
                new StorageManifestDto(1, "0.1.0", null, List.of("profile", "settings", "configuration", "knowledge", "integrations")),
                new StorageProfileDto(user.getId().toString(), user.getEmail(), user.getName(), user.getProvider()),
                new StorageSettingsDto(List.of(new StorageConnectedAccountDto("google", "sub-1", "user@example.com", "Test User",
                        new StorageTokenDto("access-token", "refresh-token", "Bearer", List.of("openid", "email"), "2026-07-03T22:00:00Z")))),
                new StorageConfigurationDto(List.of(new StorageRuleDto("SENDER", "allowed@example.com")), List.of(), List.of()),
                new StorageKnowledgeDto(List.of(), List.of(), List.of(), List.of(), List.of()),
                new StorageIntegrationsDto(new StorageSyncStateDto(null), new StorageSyncStateDto(null))
        );
    }
}
