package com.familyos.familyos.mail.repository;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.mail.entity.MailSyncState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MailSyncStateRepository extends JpaRepository<MailSyncState, UUID> {
    Optional<MailSyncState> findByAccount(OAuthAccount account);
}
