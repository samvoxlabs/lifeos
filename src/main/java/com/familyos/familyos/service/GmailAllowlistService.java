package com.familyos.familyos.service;

import com.familyos.familyos.authentication.entity.GmailAllowlistEntry;
import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.exception.UnauthorizedException;
import com.familyos.familyos.authentication.repository.GmailAllowlistRepository;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.dto.GmailAllowlistDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GmailAllowlistService {

    private static final String GOOGLE_PROVIDER = "google";

    private final UserService userService;
    private final OAuthAccountService oauthAccountService;
    private final GmailAllowlistRepository gmailAllowlistRepository;

    public GmailAllowlistService(UserService userService, OAuthAccountService oauthAccountService,
                                 GmailAllowlistRepository gmailAllowlistRepository) {
        this.userService = userService;
        this.oauthAccountService = oauthAccountService;
        this.gmailAllowlistRepository = gmailAllowlistRepository;
    }

    @Transactional(readOnly = true)
    public GmailAllowlistDto readAllowlist(String userId) {
        OAuthAccount account = resolveGoogleAccount(userId);
        List<GmailAllowlistEntry> entries = gmailAllowlistRepository.findByAccountOrderByEntryTypeAscEntryValueAsc(account);
        return toDto(entries);
    }

    @Transactional
    public void replaceAllowlist(OAuthAccount account, List<String> senders, List<String> subjects) {
        gmailAllowlistRepository.deleteByAccount(account);
        for (String sender : normalize(senders)) {
            gmailAllowlistRepository.save(new GmailAllowlistEntry(account, "SENDER", sender));
        }
        for (String subject : normalize(subjects)) {
            gmailAllowlistRepository.save(new GmailAllowlistEntry(account, "SUBJECT", subject));
        }
    }

    private GmailAllowlistDto toDto(List<GmailAllowlistEntry> entries) {
        List<String> senders = entries.stream()
                .filter(entry -> "SENDER".equalsIgnoreCase(entry.getEntryType()))
                .map(GmailAllowlistEntry::getEntryValue)
                .toList();
        List<String> subjects = entries.stream()
                .filter(entry -> "SUBJECT".equalsIgnoreCase(entry.getEntryType()))
                .map(GmailAllowlistEntry::getEntryValue)
                .toList();
        return new GmailAllowlistDto(senders, subjects);
    }

    private List<String> normalize(List<String> values) {
        return values == null ? List.of() : values.stream().filter(v -> v != null && !v.isBlank()).toList();
    }

    private OAuthAccount resolveGoogleAccount(String userId) {
        User user;
        try {
            user = userService.findById(java.util.UUID.fromString(userId))
                    .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Authenticated user not found");
        }

        return oauthAccountService.findByUserAndProvider(user, GOOGLE_PROVIDER)
                .orElseThrow(() -> new UnauthorizedException("Google account not connected"));
    }
}
