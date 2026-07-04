package com.familyos.familyos.storage;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.storage.dto.StorageBundleDto;
import com.familyos.familyos.storage.dto.StorageConfigurationDto;
import com.familyos.familyos.storage.dto.StorageIntegrationsDto;
import com.familyos.familyos.storage.dto.StorageKnowledgeDto;
import com.familyos.familyos.storage.dto.StorageManifestDto;
import com.familyos.familyos.storage.dto.StorageProfileDto;
import com.familyos.familyos.storage.dto.StorageSettingsDto;
import com.familyos.familyos.storage.dto.StorageSyncStateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class StorageBootstrapService {

    private static final List<String> DEFAULT_MODULES = List.of("profile", "settings", "configuration", "knowledge", "integrations");

    private final GoogleDriveStorageService googleDriveStorageService;
    private final ImportService importService;

    public StorageBootstrapService(GoogleDriveStorageService googleDriveStorageService, ImportService importService) {
        this.googleDriveStorageService = googleDriveStorageService;
        this.importService = importService;
    }

    @Transactional
    public StorageBundleDto bootstrap(User user, String accessToken) {
        StorageBundleDto bundle;
        if (googleDriveStorageService.storageExists(accessToken, user.getId().toString())) {
            bundle = googleDriveStorageService.readBundle(accessToken, user.getId().toString());
        } else {
            Instant now = Instant.now();
            bundle = defaultBundle(user, now);
            googleDriveStorageService.writeBundle(accessToken, user.getId().toString(), bundle);
        }

        importService.importBundle(bundle);
        return bundle;
    }

    private StorageBundleDto defaultBundle(User user, Instant timestamp) {
        return new StorageBundleDto(
                new StorageManifestDto(1, "0.1.0", timestamp.toString(), DEFAULT_MODULES),
                new StorageProfileDto(user.getId().toString(), user.getEmail(), user.getName(), user.getProvider()),
                new StorageSettingsDto(List.of()),
                new StorageConfigurationDto(List.of(), List.of(), List.of()),
                new StorageKnowledgeDto(List.of(), List.of(), List.of(), List.of(), List.of()),
                new StorageIntegrationsDto(new StorageSyncStateDto(null), new StorageSyncStateDto(null))
        );
    }
}
