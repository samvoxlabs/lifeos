package com.familyos.familyos.mail.repository;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.mail.entity.MailConflict;
import com.familyos.familyos.mail.entity.MailExtractedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MailConflictRepository extends JpaRepository<MailConflict, UUID> {
    List<MailConflict> findByExtractedEvent(MailExtractedEvent extractedEvent);
    Optional<MailConflict> findByIdAndExtractedEventMessageAccount(UUID id, OAuthAccount account);
    Optional<MailConflict> findByExtractedEventAndConflictingEventIdAndConflictingEventSource(
            MailExtractedEvent extractedEvent,
            String conflictingEventId,
            String conflictingEventSource
    );
    List<MailConflict> findByExtractedEventMessageAccount(OAuthAccount account);
    List<MailConflict> findByExtractedEventMessageAccountAndStatus(OAuthAccount account, String status);
}
