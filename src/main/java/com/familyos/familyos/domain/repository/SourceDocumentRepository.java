package com.familyos.familyos.domain.repository;

import com.familyos.familyos.domain.entity.SourceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.familyos.familyos.domain.entity.ProcessingStatus;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface SourceDocumentRepository extends JpaRepository<SourceDocument, UUID>, JpaSpecificationExecutor<SourceDocument> {
    Optional<SourceDocument> findByProviderAndExternalIdAndSourceType(String provider, String externalId, String sourceType);
    List<SourceDocument> findAllByProcessingStatusOrderByCreatedAtAsc(ProcessingStatus processingStatus);
}
