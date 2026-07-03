package com.familyos.familyos.authentication.repository;

import com.familyos.familyos.authentication.model.OAuthToken;
import com.familyos.familyos.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, UUID> {
    Optional<OAuthToken> findByUserAndProvider(User user, String provider);
    List<OAuthToken> findByUser(User user);
    void deleteByUserAndProvider(User user, String provider);
}
