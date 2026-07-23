package com.familyos.familyos.mail.repository;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.mail.entity.MailExtractedEvent;
import com.familyos.familyos.mail.entity.MailMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MailExtractedEventRepository extends JpaRepository<MailExtractedEvent, UUID> {
    Optional<MailExtractedEvent> findByMessage(MailMessage message);
    List<MailExtractedEvent> findByMessageAccountAndIdNotAndStatusIn(OAuthAccount account, UUID id, Collection<String> statuses);
    List<MailExtractedEvent> findByMessageAccountOrderByStartsAtDesc(OAuthAccount account, Pageable pageable);
    List<MailExtractedEvent> findByMessageAccountAndStatus(OAuthAccount account, String status);
    List<MailExtractedEvent> findByMessageAccountAndStartsAtLessThanOrderByStartsAtDesc(OAuthAccount account, OffsetDateTime cursor, Pageable pageable);
}
