package com.familyos.familyos.authentication.repository;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, UUID> {
    Optional<OAuthToken> findByAccount(OAuthAccount account);
    Optional<OAuthToken> findByAccountId(UUID accountId);
    void deleteByAccount(OAuthAccount account);
}
