package com.familyos.familyos.domain.repository;

import com.familyos.familyos.domain.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActionRepository extends JpaRepository<Action, UUID> {
    void deleteBySourceDocumentId(UUID sourceDocumentId);
    List<Action> findAllBySourceDocumentId(UUID sourceDocumentId);
}
