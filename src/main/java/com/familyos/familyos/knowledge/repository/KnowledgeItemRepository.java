package com.familyos.familyos.knowledge.repository;

import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.knowledge.entity.KnowledgeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeItemRepository extends JpaRepository<KnowledgeItem, UUID> {
    List<KnowledgeItem> findByUserOrderByKindAscTitleAsc(User user);
    void deleteByUser(User user);
}
