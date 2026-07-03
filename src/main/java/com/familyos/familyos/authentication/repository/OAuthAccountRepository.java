package com.familyos.familyos.authentication.repository;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, UUID> {
    Optional<OAuthAccount> findByUserAndProvider(User user, String provider);
    Optional<OAuthAccount> findByProviderAndProviderAccountId(String provider, String providerAccountId);
    List<OAuthAccount> findByUser(User user);
}
