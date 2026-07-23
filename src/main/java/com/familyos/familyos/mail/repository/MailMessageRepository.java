package com.familyos.familyos.mail.repository;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.mail.entity.MailMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MailMessageRepository extends JpaRepository<MailMessage, UUID> {
    Optional<MailMessage> findByAccountAndGmailMessageId(OAuthAccount account, String gmailMessageId);
    List<MailMessage> findByAccountOrderByReceivedAtDesc(OAuthAccount account, Pageable pageable);
    List<MailMessage> findByAccountAndReceivedAtLessThanOrderByReceivedAtDesc(
            OAuthAccount account,
            java.time.OffsetDateTime cursor,
            Pageable pageable
    );
}
