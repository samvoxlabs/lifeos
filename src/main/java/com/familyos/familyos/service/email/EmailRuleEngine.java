package com.familyos.familyos.service.email;

import com.familyos.familyos.authentication.entity.GmailAllowlistEntry;
import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.repository.GmailAllowlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class EmailRuleEngine {

    private final GmailAllowlistRepository gmailAllowlistRepository;

    public EmailRuleEngine(GmailAllowlistRepository gmailAllowlistRepository) {
        this.gmailAllowlistRepository = gmailAllowlistRepository;
    }

    @Transactional(readOnly = true)
    public List<NormalizedEmail> filterRelevantEmails(OAuthAccount account, List<NormalizedEmail> emails) {
        List<GmailAllowlistEntry> entries = gmailAllowlistRepository.findByAccountOrderByEntryTypeAscEntryValueAsc(account);
        if (entries.isEmpty()) {
            return List.of();
        }

        List<String> senderRules = entries.stream()
                .filter(entry -> "SENDER".equalsIgnoreCase(entry.getEntryType()))
                .map(GmailAllowlistEntry::getEntryValue)
                .toList();
        List<String> subjectRules = entries.stream()
                .filter(entry -> "SUBJECT".equalsIgnoreCase(entry.getEntryType()))
                .map(GmailAllowlistEntry::getEntryValue)
                .toList();

        return emails.stream()
                .filter(email -> matchesAny(email.from(), senderRules) || matchesAny(email.subject(), subjectRules))
                .toList();
    }

    private boolean matchesAny(String value, List<String> rules) {
        if (value == null || rules == null || rules.isEmpty()) {
            return false;
        }
        String normalizedValue = value.toLowerCase(Locale.ROOT);
        return rules.stream()
                .filter(rule -> rule != null && !rule.isBlank())
                .map(rule -> rule.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedValue::contains);
    }
}
