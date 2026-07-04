package com.familyos.familyos.domain.repository;

import com.familyos.familyos.domain.entity.Extraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExtractionRepository extends JpaRepository<Extraction, UUID> {
    Optional<Extraction> findBySourceDocumentId(UUID sourceDocumentId);
}
