package com.familyos.familyos.storage.repository;

import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.storage.entity.StorageDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StorageDocumentRepository extends JpaRepository<StorageDocument, UUID> {
    List<StorageDocument> findByUserAndDocumentGroupOrderByDocumentKeyAsc(User user, String documentGroup);
    Optional<StorageDocument> findByUserAndDocumentGroupAndDocumentKey(User user, String documentGroup, String documentKey);
    void deleteByUserAndDocumentGroup(User user, String documentGroup);
    void deleteByUser(User user);
}
