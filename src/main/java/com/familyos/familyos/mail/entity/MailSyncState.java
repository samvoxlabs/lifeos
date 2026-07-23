package com.familyos.familyos.mail.entity;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mail_sync_states")
public class MailSyncState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_mail_sync_states_account_id"))
    private OAuthAccount account;

    @Column(name = "last_history_id", length = 255)
    private String lastHistoryId;

    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OAuthAccount getAccount() {
        return account;
    }

    public void setAccount(OAuthAccount account) {
        this.account = account;
    }

    public String getLastHistoryId() {
        return lastHistoryId;
    }

    public void setLastHistoryId(String lastHistoryId) {
        this.lastHistoryId = lastHistoryId;
    }

    public OffsetDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(OffsetDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
