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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "gmail_allowlist_entries",
        uniqueConstraints = @UniqueConstraint(name = "uq_gmail_allowlist_entries_account_type_value", columnNames = {"account_id", "entry_type", "entry_value"}),
        indexes = @Index(name = "idx_gmail_allowlist_entries_account_id", columnList = "account_id")
)
public class GmailAllowlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_gmail_allowlist_entries_account_id"))
    private OAuthAccount account;

    @Column(name = "entry_type", nullable = false, length = 20)
    private String entryType;

    @Column(name = "entry_value", nullable = false, length = 255)
    private String entryValue;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public GmailAllowlistEntry() {
    }

    public GmailAllowlistEntry(OAuthAccount account, String entryType, String entryValue) {
        this.account = account;
        this.entryType = entryType;
        this.entryValue = entryValue;
    }

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

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getEntryValue() {
        return entryValue;
    }

    public void setEntryValue(String entryValue) {
        this.entryValue = entryValue;
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
