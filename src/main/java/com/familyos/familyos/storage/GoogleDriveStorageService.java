package com.familyos.familyos.storage;

import com.familyos.familyos.config.properties.GoogleProperties;
import com.familyos.familyos.storage.dto.StorageBundleDto;
import com.familyos.familyos.storage.dto.StorageConfigurationDto;
import com.familyos.familyos.storage.dto.StorageIntegrationsDto;
import com.familyos.familyos.storage.dto.StorageKnowledgeDto;
import com.familyos.familyos.storage.dto.StorageKnowledgeItemDto;
import com.familyos.familyos.storage.dto.StorageManifestDto;
import com.familyos.familyos.storage.dto.StoragePromptDto;
import com.familyos.familyos.storage.dto.StorageProfileDto;
import com.familyos.familyos.storage.dto.StorageRuleDto;
import com.familyos.familyos.storage.dto.StorageSettingsDto;
import com.familyos.familyos.storage.dto.StorageSyncStateDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleDriveStorageService {

    private static final String DRIVE_FOLDER_MIME = "application/vnd.google-apps.folder";
    private static final String ROOT_FOLDER = "LifeOS";
    private static final String USERS_FOLDER = "users";
    private static final String CONFIGURATION_FOLDER = "configuration";
    private static final String KNOWLEDGE_FOLDER = "knowledge";
    private static final String INTEGRATIONS_FOLDER = "integrations";
    private static final String MANIFEST_FILE = "manifest.json";
    private static final String PROFILE_FILE = "profile.json";
    private static final String SETTINGS_FILE = "settings.json";
    private static final String EMAIL_RULES_FILE = "email-rules.json";
    private static final String CALENDAR_RULES_FILE = "calendar-rules.json";
    private static final String PROMPTS_FILE = "prompts.json";
    private static final String SUMMARIES_FILE = "summaries.json";
    private static final String ACTION_ITEMS_FILE = "action-items.json";
    private static final String REMINDERS_FILE = "reminders.json";
    private static final String RELATIONSHIPS_FILE = "relationships.json";
    private static final String MEMORIES_FILE = "memories.json";
    private static final String GMAIL_SYNC_FILE = "gmail-sync.json";
    private static final String CALENDAR_SYNC_FILE = "calendar-sync.json";

    private final RestClient restClient;
    private final RestClient uploadRestClient;
    private final ObjectMapper objectMapper;

    public GoogleDriveStorageService(GoogleProperties googleProperties, RestClient.Builder restClientBuilder,
                                     ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.baseUrl(googleProperties.apis().driveBaseUrl()).build();
        this.uploadRestClient = restClientBuilder.baseUrl("https://www.googleapis.com/upload/drive/v3").build();
        this.objectMapper = objectMapper;
    }

    public StorageBundleDto readBundle(String accessToken, String userId) {
        String rootFolder = requireFolder(accessToken, null, ROOT_FOLDER);
        String usersFolder = requireFolder(accessToken, rootFolder, USERS_FOLDER);
        String userFolder = requireFolder(accessToken, usersFolder, userId);

        StorageManifestDto manifest = readRequiredJson(accessToken, userFolder, MANIFEST_FILE, StorageManifestDto.class);
        StorageProfileDto profile = readRequiredJson(accessToken, userFolder, PROFILE_FILE, StorageProfileDto.class);
        StorageSettingsDto settings = readRequiredJson(accessToken, userFolder, SETTINGS_FILE, StorageSettingsDto.class);

        String configurationFolder = findFolderId(accessToken, userFolder, CONFIGURATION_FOLDER);
        String knowledgeFolder = findFolderId(accessToken, userFolder, KNOWLEDGE_FOLDER);
        String integrationsFolder = findFolderId(accessToken, userFolder, INTEGRATIONS_FOLDER);

        StorageConfigurationDto configuration = new StorageConfigurationDto(
                readOptionalList(accessToken, configurationFolder, EMAIL_RULES_FILE, new TypeReference<List<StorageRuleDto>>() {}),
                readOptionalList(accessToken, configurationFolder, CALENDAR_RULES_FILE, new TypeReference<List<StorageRuleDto>>() {}),
                readOptionalList(accessToken, configurationFolder, PROMPTS_FILE, new TypeReference<List<StoragePromptDto>>() {})
        );

        StorageKnowledgeDto knowledge = new StorageKnowledgeDto(
                readOptionalList(accessToken, knowledgeFolder, SUMMARIES_FILE, new TypeReference<List<StorageKnowledgeItemDto>>() {}),
                readOptionalList(accessToken, knowledgeFolder, ACTION_ITEMS_FILE, new TypeReference<List<StorageKnowledgeItemDto>>() {}),
                readOptionalList(accessToken, knowledgeFolder, REMINDERS_FILE, new TypeReference<List<StorageKnowledgeItemDto>>() {}),
                readOptionalList(accessToken, knowledgeFolder, RELATIONSHIPS_FILE, new TypeReference<List<StorageKnowledgeItemDto>>() {}),
                readOptionalList(accessToken, knowledgeFolder, MEMORIES_FILE, new TypeReference<List<StorageKnowledgeItemDto>>() {})
        );

        StorageIntegrationsDto integrations = new StorageIntegrationsDto(
                readOptionalJson(accessToken, integrationsFolder, GMAIL_SYNC_FILE, StorageSyncStateDto.class, new StorageSyncStateDto(null)),
                readOptionalJson(accessToken, integrationsFolder, CALENDAR_SYNC_FILE, StorageSyncStateDto.class, new StorageSyncStateDto(null))
        );

        return new StorageBundleDto(manifest, profile, settings, configuration, knowledge, integrations);
    }

    public boolean storageExists(String accessToken, String userId) {
        String rootFolder = findFolderId(accessToken, null, ROOT_FOLDER);
        if (rootFolder == null) {
            return false;
        }
        String usersFolder = findFolderId(accessToken, rootFolder, USERS_FOLDER);
        if (usersFolder == null) {
            return false;
        }
        String userFolder = findFolderId(accessToken, usersFolder, userId);
        return userFolder != null && findFileId(accessToken, userFolder, MANIFEST_FILE) != null;
    }

    public void writeBundle(String accessToken, String userId, StorageBundleDto bundle) {
        String rootFolder = resolveOrCreateFolder(accessToken, null, ROOT_FOLDER);
        String usersFolder = resolveOrCreateFolder(accessToken, rootFolder, USERS_FOLDER);
        String userFolder = resolveOrCreateFolder(accessToken, usersFolder, userId);
        String configurationFolder = resolveOrCreateFolder(accessToken, userFolder, CONFIGURATION_FOLDER);
        String knowledgeFolder = resolveOrCreateFolder(accessToken, userFolder, KNOWLEDGE_FOLDER);
        String integrationsFolder = resolveOrCreateFolder(accessToken, userFolder, INTEGRATIONS_FOLDER);

        writeJsonFile(accessToken, userFolder, MANIFEST_FILE, bundle.manifest());
        writeJsonFile(accessToken, userFolder, PROFILE_FILE, bundle.profile());
        writeJsonFile(accessToken, userFolder, SETTINGS_FILE, bundle.settings());

        writeJsonFile(accessToken, configurationFolder, EMAIL_RULES_FILE, bundle.configuration().emailRules());
        writeJsonFile(accessToken, configurationFolder, CALENDAR_RULES_FILE, bundle.configuration().calendarRules());
        writeJsonFile(accessToken, configurationFolder, PROMPTS_FILE, bundle.configuration().prompts());

        writeJsonFile(accessToken, knowledgeFolder, SUMMARIES_FILE, bundle.knowledge().summaries());
        writeJsonFile(accessToken, knowledgeFolder, ACTION_ITEMS_FILE, bundle.knowledge().actionItems());
        writeJsonFile(accessToken, knowledgeFolder, REMINDERS_FILE, bundle.knowledge().reminders());
        writeJsonFile(accessToken, knowledgeFolder, RELATIONSHIPS_FILE, bundle.knowledge().relationships());
        writeJsonFile(accessToken, knowledgeFolder, MEMORIES_FILE, bundle.knowledge().memories());

        writeJsonFile(accessToken, integrationsFolder, GMAIL_SYNC_FILE, bundle.integrations().gmailSync());
        writeJsonFile(accessToken, integrationsFolder, CALENDAR_SYNC_FILE, bundle.integrations().calendarSync());
    }

    private <T> T readRequiredJson(String accessToken, String parentId, String fileName, Class<T> type) {
        return readOptionalJson(accessToken, parentId, fileName, type, null);
    }

    private <T> T readOptionalJson(String accessToken, String parentId, String fileName, Class<T> type, T defaultValue) {
        if (parentId == null) {
            return defaultValue;
        }
        String fileId = findFileId(accessToken, parentId, fileName);
        if (fileId == null) {
            if (defaultValue != null) {
                return defaultValue;
            }
            throw new IllegalArgumentException("Missing Drive file: " + fileName);
        }
        String content = readTextFile(accessToken, fileId);
        try {
            return objectMapper.readValue(content, type);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to parse Drive file " + fileName, ex);
        }
    }

    private <T> List<T> readOptionalList(String accessToken, String parentId, String fileName, TypeReference<List<T>> typeReference) {
        if (parentId == null) {
            return List.of();
        }
        String fileId = findFileId(accessToken, parentId, fileName);
        if (fileId == null) {
            return List.of();
        }
        String content = readTextFile(accessToken, fileId);
        try {
            return objectMapper.readValue(content, typeReference);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to parse Drive file " + fileName, ex);
        }
    }

    private String requireFolder(String accessToken, String parentId, String name) {
        String folderId = findFolderId(accessToken, parentId, name);
        if (folderId == null) {
            throw new IllegalArgumentException("Missing Drive folder: " + name);
        }
        return folderId;
    }

    private String resolveOrCreateFolder(String accessToken, String parentId, String name) {
        String folderId = findFolderId(accessToken, parentId, name);
        if (folderId != null) {
            return folderId;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", name);
        payload.put("mimeType", DRIVE_FOLDER_MIME);
        if (parentId != null) {
            payload.put("parents", List.of(parentId));
        }
        Map<String, Object> response = restClient.post()
                .uri("/files")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Map.class);
        return response == null ? "" : String.valueOf(response.getOrDefault("id", ""));
    }

    private String findFolderId(String accessToken, String parentId, String name) {
        List<Map<String, Object>> files = searchFiles(accessToken, buildQuery(parentId, name, true));
        return files.isEmpty() ? null : String.valueOf(files.get(0).get("id"));
    }

    private String findFileId(String accessToken, String parentId, String name) {
        List<Map<String, Object>> files = searchFiles(accessToken, buildQuery(parentId, name, false));
        return files.isEmpty() ? null : String.valueOf(files.get(0).get("id"));
    }

    private List<Map<String, Object>> searchFiles(String accessToken, String query) {
        Map<String, Object> response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/files")
                        .queryParam("pageSize", 10)
                        .queryParam("q", query)
                        .queryParam("fields", "files(id,name,mimeType,modifiedTime,webViewLink,size)")
                        .build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);
        return response == null ? List.of() : (List<Map<String, Object>>) response.getOrDefault("files", List.of());
    }

    private String buildQuery(String parentId, String name, boolean folder) {
        StringBuilder query = new StringBuilder();
        if (folder) {
            query.append("mimeType = '").append(DRIVE_FOLDER_MIME).append("' and ");
        } else {
            query.append("mimeType != '").append(DRIVE_FOLDER_MIME).append("' and ");
        }
        query.append("name = '").append(name.replace("'", "\\'")).append("' and trashed = false");
        if (parentId != null) {
            query.append(" and '").append(parentId).append("' in parents");
        }
        return query.toString();
    }

    private String readTextFile(String accessToken, String fileId) {
        return restClient.get()
                .uri("/files/{fileId}?alt=media", fileId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(String.class);
    }

    private void writeJsonFile(String accessToken, String parentId, String fileName, Object value) {
        String existingId = findFileId(accessToken, parentId, fileName);
        if (existingId != null) {
            restClient.delete()
                    .uri("/files/{fileId}", existingId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to serialize Drive file " + fileName, ex);
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", fileName);
        metadata.put("mimeType", "application/json");
        metadata.put("parents", List.of(parentId));

        Map<String, Object> created = restClient.post()
                .uri("/files")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(metadata)
                .retrieve()
                .body(Map.class);

        String fileId = created == null ? "" : String.valueOf(created.getOrDefault("id", ""));
        if (fileId.isBlank()) {
            throw new IllegalArgumentException("Unable to create Drive file " + fileName);
        }

        uploadRestClient.patch()
                .uri("/files/{fileId}?uploadType=media", fileId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .body(Map.class);
    }
}
