package com.familyos.familyos.storage;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.OAuthTokenService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.knowledge.entity.KnowledgeItem;
import com.familyos.familyos.knowledge.repository.KnowledgeItemRepository;
import com.familyos.familyos.service.GmailAllowlistService;
import com.familyos.familyos.storage.dto.StorageBundleDto;
import com.familyos.familyos.storage.dto.StorageConfigurationDto;
import com.familyos.familyos.storage.dto.StorageConnectedAccountDto;
import com.familyos.familyos.storage.dto.StorageIntegrationsDto;
import com.familyos.familyos.storage.dto.StorageKnowledgeItemDto;
import com.familyos.familyos.storage.dto.StoragePromptDto;
import com.familyos.familyos.storage.dto.StorageRuleDto;
import com.familyos.familyos.storage.dto.StorageTokenDto;
import com.familyos.familyos.storage.entity.StorageDocument;
import com.familyos.familyos.storage.repository.StorageDocumentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class ImportService {

    private static final String CONFIGURATION = "configuration";
    private static final String INTEGRATIONS = "integrations";
    private static final String EMAIL_RULES = "email-rules";
    private static final String CALENDAR_RULES = "calendar-rules";
    private static final String PROMPTS = "prompts";
    private static final String GMAIL_SYNC = "gmail-sync";
    private static final String CALENDAR_SYNC = "calendar-sync";

    private final UserService userService;
    private final OAuthAccountService oauthAccountService;
    private final OAuthTokenService oauthTokenService;
    private final GmailAllowlistService gmailAllowlistService;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final StorageDocumentRepository storageDocumentRepository;
    private final ObjectMapper objectMapper;

    public ImportService(UserService userService, OAuthAccountService oauthAccountService,
                         OAuthTokenService oauthTokenService, GmailAllowlistService gmailAllowlistService,
                         KnowledgeItemRepository knowledgeItemRepository,
                         StorageDocumentRepository storageDocumentRepository, ObjectMapper objectMapper) {
        this.userService = userService;
        this.oauthAccountService = oauthAccountService;
        this.oauthTokenService = oauthTokenService;
        this.gmailAllowlistService = gmailAllowlistService;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.storageDocumentRepository = storageDocumentRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void importBundle(StorageBundleDto bundle) {
        User user = importUser(bundle);
        importAccounts(user, bundle.settings() == null ? List.of() : bundle.settings().connectedAccounts());
        importConfiguration(user, bundle.configuration());
        importKnowledge(user, bundle.knowledge());
        importIntegrations(user, bundle.integrations());
    }

    private User importUser(StorageBundleDto bundle) {
        if (bundle.profile() == null) {
            throw new IllegalArgumentException("Storage profile is required");
        }
        return userService.findOrCreateUser(bundle.profile().email(), bundle.profile().name(), bundle.profile().provider());
    }

    private void importAccounts(User user, List<StorageConnectedAccountDto> connectedAccounts) {
        if (connectedAccounts == null) {
            return;
        }
        for (StorageConnectedAccountDto accountDto : connectedAccounts) {
            OAuthAccount account = oauthAccountService.findOrCreateAccount(
                    user,
                    accountDto.provider(),
                    accountDto.providerAccountId(),
                    accountDto.email(),
                    accountDto.displayName()
            );

            StorageTokenDto tokenDto = accountDto.token();
            if (tokenDto != null && tokenDto.accessToken() != null && !tokenDto.accessToken().isBlank()) {
                oauthTokenService.saveToken(
                        account,
                        tokenDto.accessToken(),
                        tokenDto.refreshToken(),
                        tokenDto.tokenType(),
                        tokenDto.scopes() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tokenDto.scopes()),
                        tokenDto.expiresAt() == null ? null : LocalDateTime.ofInstant(Instant.parse(tokenDto.expiresAt()), ZoneOffset.UTC)
                );
            }
        }
    }

    private void importConfiguration(User user, StorageConfigurationDto configuration) {
        storageDocumentRepository.deleteByUserAndDocumentGroup(user, CONFIGURATION);
        OAuthAccount googleAccount = oauthAccountService.findByUserAndProvider(user, "google").orElse(null);
        if (googleAccount != null) {
            List<String> senders = configuration == null || configuration.emailRules() == null ? List.of() : configuration.emailRules().stream()
                    .filter(rule -> "SENDER".equalsIgnoreCase(rule.type()))
                    .map(StorageRuleDto::value)
                    .toList();
            List<String> subjects = configuration == null || configuration.emailRules() == null ? List.of() : configuration.emailRules().stream()
                    .filter(rule -> "SUBJECT".equalsIgnoreCase(rule.type()))
                    .map(StorageRuleDto::value)
                    .toList();
            gmailAllowlistService.replaceAllowlist(googleAccount, senders, subjects);
        }

        if (configuration == null) {
            return;
        }

        writeDocument(user, CONFIGURATION, EMAIL_RULES, configuration.emailRules());
        writeDocument(user, CONFIGURATION, CALENDAR_RULES, configuration.calendarRules());
        writeDocument(user, CONFIGURATION, PROMPTS, configuration.prompts());
    }

    private void importKnowledge(User user, com.familyos.familyos.storage.dto.StorageKnowledgeDto knowledge) {
        knowledgeItemRepository.deleteByUser(user);
        if (knowledge == null) {
            return;
        }
        saveKnowledgeItems(user, "SUMMARY", knowledge.summaries());
        saveKnowledgeItems(user, "ACTION_ITEM", knowledge.actionItems());
        saveKnowledgeItems(user, "REMINDER", knowledge.reminders());
        saveKnowledgeItems(user, "RELATIONSHIP", knowledge.relationships());
        saveKnowledgeItems(user, "MEMORY", knowledge.memories());
    }

    private void importIntegrations(User user, StorageIntegrationsDto integrations) {
        storageDocumentRepository.deleteByUserAndDocumentGroup(user, INTEGRATIONS);
        if (integrations == null) {
            return;
        }
        writeDocument(user, INTEGRATIONS, GMAIL_SYNC, integrations.gmailSync());
        writeDocument(user, INTEGRATIONS, CALENDAR_SYNC, integrations.calendarSync());
    }

    private void saveKnowledgeItems(User user, String kind, List<StorageKnowledgeItemDto> items) {
        if (items == null) {
            return;
        }
        for (StorageKnowledgeItemDto item : items) {
            KnowledgeItem knowledgeItem = new KnowledgeItem();
            knowledgeItem.setUser(user);
            knowledgeItem.setKind(kind);
            knowledgeItem.setTitle(item.title());
            knowledgeItem.setContent(item.content());
            knowledgeItem.setSource(item.source());
            knowledgeItem.setMetadata(item.metadata());
            knowledgeItemRepository.save(knowledgeItem);
        }
    }

    private void writeDocument(User user, String group, String key, Object value) {
        StorageDocument document = storageDocumentRepository
                .findByUserAndDocumentGroupAndDocumentKey(user, group, key)
                .orElseGet(StorageDocument::new);
        document.setUser(user);
        document.setDocumentGroup(group);
        document.setDocumentKey(key);
        document.setContentJson(serialize(value));
        storageDocumentRepository.save(document);
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize storage document", ex);
        }
    }
}
