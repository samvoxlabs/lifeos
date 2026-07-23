package com.familyos.familyos.demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.mail.exception.MailApiException;
import com.familyos.familyos.authentication.repository.OAuthAccountRepository;
import com.familyos.familyos.authentication.repository.UserRepository;
import com.familyos.familyos.authentication.service.OAuthTokenService;
import com.familyos.familyos.authentication.service.TokenRefreshService;
import com.familyos.familyos.demo.mapper.DemoEmailMimeMapper;
import com.familyos.familyos.integrations.google.gmail.GoogleGmailMailboxAdapter;
import com.familyos.familyos.mail.entity.MailMessage;
import com.familyos.familyos.mail.repository.MailMessageRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DemoGmailService {

    private static final Logger log = LoggerFactory.getLogger(DemoGmailService.class);
    private static final String GMAIL_MODIFY_SCOPE = "https://www.googleapis.com/auth/gmail.modify";

    private final MailMessageRepository mailMessageRepository;
    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final OAuthTokenService oAuthTokenService;
    private final TokenRefreshService tokenRefreshService;
    private final GoogleGmailMailboxAdapter gmailMailboxAdapter;
    private final DemoEmailMimeMapper mimeMapper;
    private final ObjectMapper objectMapper;

    public DemoGmailService(MailMessageRepository mailMessageRepository,
                            UserRepository userRepository,
                            OAuthAccountRepository oAuthAccountRepository,
                            OAuthTokenService oAuthTokenService,
                            TokenRefreshService tokenRefreshService,
                            GoogleGmailMailboxAdapter gmailMailboxAdapter,
                            DemoEmailMimeMapper mimeMapper,
                            ObjectMapper objectMapper) {
        this.mailMessageRepository = mailMessageRepository;
        this.userRepository = userRepository;
        this.oAuthAccountRepository = oAuthAccountRepository;
        this.oAuthTokenService = oAuthTokenService;
        this.tokenRefreshService = tokenRefreshService;
        this.gmailMailboxAdapter = gmailMailboxAdapter;
        this.mimeMapper = mimeMapper;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public int loadDemoEmails(String userId, String fileName) {
        OAuthAccount account = resolveAccount(userId);
        String accessToken = resolveAccessToken(account);

        List<Map<String, Object>> emails = loadScenarioFile(fileName);
        int count = 0;

        for (Map<String, Object> emailDef : emails) {
            String demoId = (String) emailDef.getOrDefault("gmailMessageId", "demo-" + UUID.randomUUID());

            if (mailMessageRepository.findByAccountAndGmailMessageId(account, demoId).isPresent()) {
                log.debug("Skipping already-loaded demo message: {}", demoId);
                count++;
                continue;
            }

            String realGmailId = null;
            try {
                String mimeBase64 = mimeMapper.toBase64UrlMime(emailDef);
                realGmailId = gmailMailboxAdapter.insertMessage(accessToken, mimeBase64, List.of("INBOX"));
                log.info("Inserted demo message into Gmail: {}", realGmailId);
            } catch (Exception e) {
                log.warn("Could not insert into Gmail ({}), storing in DB only: {}", e.getMessage(), demoId);
            }

            MailMessage message = new MailMessage();
            message.setAccount(account);
            message.setGmailMessageId(realGmailId != null ? realGmailId : demoId);
            message.setThreadId((String) emailDef.get("threadId"));

            Object fromObj = emailDef.get("from");
            if (fromObj instanceof Map<?, ?> from) {
                message.setSenderName((String) from.get("name"));
                message.setSenderEmail((String) from.get("email"));
            }

            Object toObj = emailDef.get("to");
            if (toObj instanceof List<?> toList) {
                message.setRecipients(toList.stream().map(Object::toString).toList());
            }

            message.setSubject((String) emailDef.get("subject"));
            message.setSnippet((String) emailDef.get("snippet"));
            message.setBodyText((String) emailDef.get("bodyText"));

            String receivedAtStr = (String) emailDef.get("receivedAt");
            message.setReceivedAt(receivedAtStr != null ? OffsetDateTime.parse(receivedAtStr) : OffsetDateTime.now());

            Object labelsObj = emailDef.get("labels");
            if (labelsObj instanceof List<?> labels) {
                message.setLabels(labels.stream().map(Object::toString).toList());
            } else {
                message.setLabels(List.of("INBOX"));
            }

            message.setRead(false);
            message.setHasActionableEvent(false);
            mailMessageRepository.save(message);
            count++;
        }

        return count;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Map<String, Object> insertDemoEmail(String userId, Map<String, Object> emailDef) {
        if (emailDef == null || emailDef.isEmpty()) {
            throw new IllegalArgumentException("Email request body is required");
        }

        OAuthAccount account = resolveAccount(userId);
        String accessToken = resolveAccessToken(account);

        String realGmailId = null;
        try {
            String mimeBase64 = mimeMapper.toBase64UrlMime(emailDef);
            realGmailId = gmailMailboxAdapter.insertMessage(accessToken, mimeBase64, List.of("INBOX"));
            log.info("Inserted demo email into Gmail: {}", realGmailId);
        } catch (Exception e) {
            log.warn("Could not insert into Gmail, storing in DB only: {}", e.getMessage());
        }

        String storedGmailId = realGmailId != null && !realGmailId.isBlank()
                ? realGmailId
                : "demo-" + UUID.randomUUID();
        MailMessage message = createMailMessage(account, emailDef, storedGmailId);
        mailMessageRepository.save(message);

        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("status", "READY");
        response.put("gmailMessageId", storedGmailId);
        response.put("subject", message.getSubject());
        response.put("receivedAt", message.getReceivedAt() != null ? message.getReceivedAt().toString() : null);
        return response;
    }

    @Transactional
    public int resetDemoMailbox(String userId) {
        OAuthAccount account = resolveAccount(userId);
        OAuthToken token = oAuthTokenService.findByAccount(account)
                .orElseThrow(() -> new MailApiException(HttpStatus.UNAUTHORIZED, "GMAIL_AUTH_EXPIRED", "Google token not available", false));
        if (token.scopeSet() == null || !token.scopeSet().contains(GMAIL_MODIFY_SCOPE)) {
            throw new MailApiException(
                    HttpStatus.FORBIDDEN,
                    "GMAIL_SCOPE_REQUIRED",
                    "Missing required Gmail scope: https://www.googleapis.com/auth/gmail.modify. Reconnect Google to grant inbox delete access.",
                    false
            );
        }
        String accessToken = resolveAccessToken(account);

        List<String> remoteMessageIds = gmailMailboxAdapter.fetchMessages(accessToken, 1000, "in:inbox").stream()
                .map(message -> message.gmailMessageId())
                .toList();
        for (String messageId : remoteMessageIds) {
            gmailMailboxAdapter.trashMessage(accessToken, messageId);
        }

        List<MailMessage> messages = mailMessageRepository.findByAccountOrderByReceivedAtDesc(account, Pageable.unpaged());
        for (MailMessage message : messages) {
            gmailMailboxAdapter.trashMessage(accessToken, message.getGmailMessageId());
        }
        if (!messages.isEmpty()) {
            mailMessageRepository.deleteAll(messages);
        }

        log.info("Demo mailbox reset for user {}: {} Gmail messages trashed, {} DB messages cleared", userId, remoteMessageIds.size(), messages.size());
        return Math.max(remoteMessageIds.size(), messages.size());
    }

    public List<Map<String, Object>> getGmailMessages(String userId) {
        OAuthAccount account = resolveAccount(userId);
        List<MailMessage> messages = mailMessageRepository
                .findByAccountOrderByReceivedAtDesc(account, Pageable.ofSize(50));

        return messages.stream().map(msg -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", msg.getGmailMessageId());
            m.put("threadId", msg.getThreadId());
            m.put("from", Map.of("name", msg.getSenderName() != null ? msg.getSenderName() : "",
                                 "email", msg.getSenderEmail() != null ? msg.getSenderEmail() : ""));
            m.put("to", msg.getRecipients() != null ? msg.getRecipients() : List.of());
            m.put("subject", msg.getSubject());
            m.put("snippet", msg.getSnippet());
            m.put("receivedAt", msg.getReceivedAt() != null ? msg.getReceivedAt().toString() : null);
            m.put("read", msg.isRead());
            m.put("labels", msg.getLabels() != null ? msg.getLabels() : List.of());
            return m;
        }).toList();
    }

    private List<Map<String, Object>> loadScenarioFile(String fileName) {
        String path = "demo/" + (fileName.endsWith(".json") ? fileName : fileName + ".json");
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Could not load demo scenario: " + path, e);
        }
    }

    private OAuthAccount resolveAccount(String userId) {
        User user;
        try {
            user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid userId: " + userId);
        }
        List<OAuthAccount> accounts = oAuthAccountRepository.findByUser(user);
        if (accounts.isEmpty()) {
            throw new IllegalArgumentException("No OAuth account for user: " + userId);
        }
        return accounts.get(0);
    }

    private MailMessage createMailMessage(OAuthAccount account, Map<String, Object> emailDef, String gmailMessageId) {
        MailMessage message = new MailMessage();
        message.setAccount(account);
        message.setGmailMessageId(gmailMessageId);
        message.setThreadId((String) emailDef.get("threadId"));

        Object fromObj = emailDef.get("from");
        if (fromObj instanceof Map<?, ?> from) {
            message.setSenderName((String) from.get("name"));
            message.setSenderEmail((String) from.get("email"));
        }

        Object toObj = emailDef.get("to");
        if (toObj instanceof List<?> toList) {
            message.setRecipients(toList.stream().map(Object::toString).toList());
        }

        message.setSubject((String) emailDef.get("subject"));
        message.setSnippet((String) emailDef.get("snippet"));
        message.setBodyText((String) emailDef.getOrDefault("bodyText", emailDef.get("snippet")));

        String receivedAtStr = (String) emailDef.get("receivedAt");
        message.setReceivedAt(receivedAtStr != null ? OffsetDateTime.parse(receivedAtStr) : OffsetDateTime.now());

        Object labelsObj = emailDef.get("labels");
        if (labelsObj instanceof List<?> labels) {
            message.setLabels(labels.stream().map(Object::toString).toList());
        } else {
            message.setLabels(List.of("INBOX"));
        }

        message.setRead(false);
        message.setHasActionableEvent(false);
        return message;
    }

    private String resolveAccessToken(OAuthAccount account) {
        OAuthToken token = oAuthTokenService.findByAccount(account)
                .orElseThrow(() -> new IllegalArgumentException("No token for account"));
        return tokenRefreshService.getValidAccessToken(token);
    }
}
