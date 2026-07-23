package com.familyos.familyos.mail.repository;

import com.familyos.familyos.mail.entity.MailConflict;
import com.familyos.familyos.mail.entity.MailResolutionExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MailResolutionExecutionRepository extends JpaRepository<MailResolutionExecution, UUID> {
    Optional<MailResolutionExecution> findByConflictAndActionKey(MailConflict conflict, String actionKey);
    Optional<MailResolutionExecution> findByIdempotencyKey(String idempotencyKey);
}
