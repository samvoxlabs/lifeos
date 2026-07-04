package com.familyos.familyos.storage;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.exception.UnauthorizedException;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.OAuthTokenService;
import com.familyos.familyos.authentication.service.TokenRefreshService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.dto.AuthenticatedUser;
import com.familyos.familyos.storage.dto.StorageBundleDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class StorageServiceImpl implements StorageService {

    private final UserService userService;
    private final OAuthAccountService oauthAccountService;
    private final OAuthTokenService oauthTokenService;
    private final TokenRefreshService tokenRefreshService;
    private final StorageBootstrapService storageBootstrapService;
    private final ExportService exportService;
    private final GoogleDriveStorageService googleDriveStorageService;
    private final StorageState storageState;
    private final ObjectMapper objectMapper;

    public StorageServiceImpl(UserService userService, OAuthAccountService oauthAccountService,
                              OAuthTokenService oauthTokenService, TokenRefreshService tokenRefreshService,
                              StorageBootstrapService storageBootstrapService, ExportService exportService,
                              GoogleDriveStorageService googleDriveStorageService, StorageState storageState,
                              ObjectMapper objectMapper) {
        this.userService = userService;
        this.oauthAccountService = oauthAccountService;
        this.oauthTokenService = oauthTokenService;
        this.tokenRefreshService = tokenRefreshService;
        this.storageBootstrapService = storageBootstrapService;
        this.exportService = exportService;
        this.googleDriveStorageService = googleDriveStorageService;
        this.storageState = storageState;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public StorageStatus bootstrap(String userId) {
        User user = resolveUser(userId);
        OAuthAccount account = resolveGoogleAccount(user);
        String accessToken = resolveAccessToken(account);

        Instant now = Instant.now();
        StorageBundleDto bundle = storageBootstrapService.bootstrap(user, accessToken);
        storageState.markBootstrap(now, resolveLastSave(bundle), hash(bundle));
        storageState.updateSchemaVersion(bundle.manifest().schemaVersion());
        storageState.updateAvailableModules(bundle.manifest().modules());
        return status(userId);
    }

    @Override
    @Transactional
    public StorageStatus save(String userId) {
        User user = resolveUser(userId);
        OAuthAccount account = resolveGoogleAccount(user);
        String accessToken = resolveAccessToken(account);

        Instant savedAt = Instant.now();
        StorageBundleDto bundle = exportService.exportBundle(user.getId().toString(), savedAt);
        googleDriveStorageService.writeBundle(accessToken, user.getId().toString(), bundle);

        String hash = hash(bundle);
        storageState.markSaved(savedAt, hash);
        storageState.updateSchemaVersion(bundle.manifest().schemaVersion());
        storageState.updateAvailableModules(bundle.manifest().modules());
        return status(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public StorageStatus status(String userId) {
        User user = resolveUser(userId);
        StorageBundleDto current = exportService.exportBundle(user.getId().toString(), storageState.lastSavedAt());
        String currentHash = hash(current);
        return new StorageStatus(
                new AuthenticatedUser(user.getId().toString(), user.getEmail(), user.getName(), user.getProvider()),
                storageState.initialized(),
                storageState.schemaVersion(),
                storageState.availableModules(),
                storageState.lastLoadedAt() == null ? null : storageState.lastLoadedAt().toString(),
                storageState.lastSavedAt() == null ? null : storageState.lastSavedAt().toString(),
                storageState.isDirty(currentHash)
        );
    }

    private User resolveUser(String userId) {
        try {
            return userService.findById(java.util.UUID.fromString(userId))
                    .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Authenticated user not found");
        }
    }

    private OAuthAccount resolveGoogleAccount(User user) {
        return oauthAccountService.findByUserAndProvider(user, "google")
                .orElseThrow(() -> new UnauthorizedException("Google account not connected"));
    }

    private String resolveAccessToken(OAuthAccount account) {
        OAuthToken oauthToken = oauthTokenService.findByAccount(account)
                .orElseThrow(() -> new UnauthorizedException("Google token not available"));
        return tokenRefreshService.getValidAccessToken(oauthToken);
    }

    private String hash(StorageBundleDto bundle) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(normalizeForHash(bundle));
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (JsonProcessingException | java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to hash storage snapshot", ex);
        }
    }

    private StorageBundleDto normalizeForHash(StorageBundleDto bundle) {
        return new StorageBundleDto(
                new com.familyos.familyos.storage.dto.StorageManifestDto(
                        bundle.manifest().schemaVersion(),
                        bundle.manifest().lifeOSVersion(),
                        null,
                        bundle.manifest().modules()
                ),
                bundle.profile(),
                bundle.settings(),
                bundle.configuration(),
                bundle.knowledge(),
                bundle.integrations()
        );
    }

    private Instant resolveLastSave(StorageBundleDto bundle) {
        if (bundle.manifest() == null || bundle.manifest().lastSaved() == null || bundle.manifest().lastSaved().isBlank()) {
            return Instant.now();
        }
        return Instant.parse(bundle.manifest().lastSaved());
    }
}
