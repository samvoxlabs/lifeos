package com.familyos.familyos.storage;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.repository.OAuthAccountRepository;
import com.familyos.familyos.authentication.repository.OAuthTokenRepository;
import com.familyos.familyos.authentication.repository.UserRepository;
import com.familyos.familyos.knowledge.repository.KnowledgeItemRepository;
import com.familyos.familyos.storage.dto.StorageBundleDto;
import com.familyos.familyos.storage.dto.StorageConfigurationDto;
import com.familyos.familyos.storage.dto.StorageConnectedAccountDto;
import com.familyos.familyos.storage.dto.StorageIntegrationsDto;
import com.familyos.familyos.storage.dto.StorageKnowledgeDto;
import com.familyos.familyos.storage.dto.StorageKnowledgeItemDto;
import com.familyos.familyos.storage.dto.StorageManifestDto;
import com.familyos.familyos.storage.dto.StoragePromptDto;
import com.familyos.familyos.storage.dto.StorageProfileDto;
import com.familyos.familyos.storage.dto.StorageRuleDto;
import com.familyos.familyos.storage.dto.StorageSettingsDto;
import com.familyos.familyos.storage.dto.StorageSyncStateDto;
import com.familyos.familyos.storage.dto.StorageTokenDto;
import com.familyos.familyos.storage.entity.StorageDocument;
import com.familyos.familyos.storage.repository.StorageDocumentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class ExportService {

    private static final int SCHEMA_VERSION = 1;
    private static final String LIFE_OS_VERSION = "0.1.0";
    private static final String CONFIGURATION = "configuration";
    private static final String INTEGRATIONS = "integrations";
    private static final String EMAIL_RULES = "email-rules";
    private static final String CALENDAR_RULES = "calendar-rules";
    private static final String PROMPTS = "prompts";
    private static final String GMAIL_SYNC = "gmail-sync";
    private static final String CALENDAR_SYNC = "calendar-sync";

    private final UserRepository userRepository;
    private final OAuthAccountRepository oauthAccountRepository;
    private final OAuthTokenRepository oauthTokenRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final StorageDocumentRepository storageDocumentRepository;
    private final ObjectMapper objectMapper;

    public ExportService(UserRepository userRepository, OAuthAccountRepository oauthAccountRepository,
                         OAuthTokenRepository oauthTokenRepository, KnowledgeItemRepository knowledgeItemRepository,
                         StorageDocumentRepository storageDocumentRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.oauthAccountRepository = oauthAccountRepository;
        this.oauthTokenRepository = oauthTokenRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.storageDocumentRepository = storageDocumentRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public StorageBundleDto exportBundle(String userId, Instant lastSavedAt) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<OAuthAccount> accounts = oauthAccountRepository.findByUser(user);
        List<StorageConnectedAccountDto> connectedAccounts = accounts.stream()
                .map(account -> new StorageConnectedAccountDto(
                        account.getProvider(),
                        account.getProviderAccountId(),
                        account.getEmail(),
                        account.getDisplayName(),
                        oauthTokenRepository.findByAccount(account)
                                .map(this::toTokenDto)
                                .orElse(null)
                ))
                .toList();

        StorageConfigurationDto configuration = new StorageConfigurationDto(
                readDocumentList(user, CONFIGURATION, EMAIL_RULES, new TypeReference<List<StorageRuleDto>>() {}),
                readDocumentList(user, CONFIGURATION, CALENDAR_RULES, new TypeReference<List<StorageRuleDto>>() {}),
                readDocumentList(user, CONFIGURATION, PROMPTS, new TypeReference<List<StoragePromptDto>>() {})
        );

        List<StorageKnowledgeItemDto> knowledgeItems = knowledgeItemRepository.findByUserOrderByKindAscTitleAsc(user).stream()
                .map(item -> new StorageKnowledgeItemDto(
                        item.getId().toString(),
                        item.getKind(),
                        item.getTitle(),
                        item.getContent(),
                        item.getSource(),
                        item.getMetadata()
                ))
                .toList();

        StorageIntegrationsDto integrations = new StorageIntegrationsDto(
                readDocument(user, INTEGRATIONS, GMAIL_SYNC, StorageSyncStateDto.class, new StorageSyncStateDto(null)),
                readDocument(user, INTEGRATIONS, CALENDAR_SYNC, StorageSyncStateDto.class, new StorageSyncStateDto(null))
        );

        return new StorageBundleDto(
                new StorageManifestDto(
                        SCHEMA_VERSION,
                        LIFE_OS_VERSION,
                        lastSavedAt == null ? null : lastSavedAt.toString(),
                        List.of("profile", "settings", "configuration", "knowledge", "integrations")
                ),
                new StorageProfileDto(
                        user.getId().toString(),
                        user.getEmail(),
                        user.getName(),
                        user.getProvider()
                ),
                new StorageSettingsDto(connectedAccounts),
                configuration,
                new StorageKnowledgeDto(
                        knowledgeItems.stream().filter(item -> "SUMMARY".equalsIgnoreCase(item.type())).toList(),
                        knowledgeItems.stream().filter(item -> "ACTION_ITEM".equalsIgnoreCase(item.type())).toList(),
                        knowledgeItems.stream().filter(item -> "REMINDER".equalsIgnoreCase(item.type())).toList(),
                        knowledgeItems.stream().filter(item -> "RELATIONSHIP".equalsIgnoreCase(item.type())).toList(),
                        knowledgeItems.stream().filter(item -> "MEMORY".equalsIgnoreCase(item.type())).toList()
                ),
                integrations
        );
    }

    private StorageTokenDto toTokenDto(OAuthToken token) {
        return new StorageTokenDto(
                token.getAccessToken(),
                token.getRefreshToken(),
                token.getTokenType(),
                token.scopeSet().stream().toList(),
                token.getExpiresAt() == null ? null : token.getExpiresAt().atZone(ZoneOffset.UTC).toInstant().toString()
        );
    }

    private <T> T readDocument(User user, String group, String key, Class<T> type, T defaultValue) {
        return storageDocumentRepository.findByUserAndDocumentGroupAndDocumentKey(user, group, key)
                .map(StorageDocument::getContentJson)
                .map(content -> deserialize(content, type))
                .orElse(defaultValue);
    }

    private <T> List<T> readDocumentList(User user, String group, String key, TypeReference<List<T>> typeReference) {
        return storageDocumentRepository.findByUserAndDocumentGroupAndDocumentKey(user, group, key)
                .map(StorageDocument::getContentJson)
                .map(content -> deserialize(content, typeReference))
                .orElse(List.of());
    }

    private <T> T deserialize(String content, Class<T> type) {
        try {
            return objectMapper.readValue(content, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to parse storage document", ex);
        }
    }

    private <T> T deserialize(String content, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(content, typeReference);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to parse storage document", ex);
        }
    }
}
