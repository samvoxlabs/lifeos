package com.familyos.familyos.authentication.repository;

import com.familyos.familyos.authentication.entity.GmailAllowlistEntry;
import com.familyos.familyos.authentication.entity.OAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GmailAllowlistRepository extends JpaRepository<GmailAllowlistEntry, UUID> {
    List<GmailAllowlistEntry> findByAccountOrderByEntryTypeAscEntryValueAsc(OAuthAccount account);
    void deleteByAccount(OAuthAccount account);
}
