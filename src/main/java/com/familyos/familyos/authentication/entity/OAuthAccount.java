package com.familyos.familyos.authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "oauth_accounts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_oauth_accounts_user_provider", columnNames = {"user_id", "provider"}),
        @UniqueConstraint(name = "uq_oauth_accounts_provider_account", columnNames = {"provider", "provider_account_id"})
    },
    indexes = {
        @Index(name = "idx_oauth_accounts_user_id", columnList = "user_id"),
        @Index(name = "idx_oauth_accounts_provider", columnList = "provider")
    }
)
public class OAuthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_oauth_accounts_user_id"))
    private User user;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_account_id", nullable = false, length = 255)
    private String providerAccountId;

    @Column(length = 255)
    private String email;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY, orphanRemoval = true)
    private OAuthToken token;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public OAuthAccount() {
    }

    public OAuthAccount(User user, String provider, String providerAccountId, String email, String displayName) {
        this.user = user;
        this.provider = provider;
        this.providerAccountId = providerAccountId;
        this.email = email;
        this.displayName = displayName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderAccountId() {
        return providerAccountId;
    }

    public void setProviderAccountId(String providerAccountId) {
        this.providerAccountId = providerAccountId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public OAuthToken getToken() {
        return token;
    }

    public void setToken(OAuthToken token) {
        this.token = token;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
