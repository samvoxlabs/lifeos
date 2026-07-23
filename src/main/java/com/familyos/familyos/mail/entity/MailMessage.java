package com.familyos.familyos.mail.entity;

import com.familyos.familyos.authentication.entity.OAuthAccount;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "mail_messages",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_mail_messages_account_gmail_message",
        columnNames = {"account_id", "gmail_message_id"}
    ),
    indexes = @Index(name = "idx_mail_messages_account_received_at", columnList = "account_id,received_at")
)
public class MailMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mail_messages_account_id"))
    private OAuthAccount account;

    @Column(name = "gmail_message_id", nullable = false, length = 255)
    private String gmailMessageId;

    @Column(name = "thread_id", length = 255)
    private String threadId;

    @Column(name = "sender_name", length = 255)
    private String senderName;

    @Column(name = "sender_email", length = 255)
    private String senderEmail;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recipients_json", columnDefinition = "jsonb")
    private List<String> recipients;

    @Column(name = "subject", length = 1000)
    private String subject;

    @Column(name = "snippet", columnDefinition = "text")
    private String snippet;

    @Column(name = "body_text", columnDefinition = "text")
    private String bodyText;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "history_id", length = 255)
    private String historyId;

    @Column(name = "has_actionable_event", nullable = false)
    private boolean hasActionableEvent;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "labels_json", columnDefinition = "jsonb")
    private List<String> labels;

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

    public String getGmailMessageId() {
        return gmailMessageId;
    }

    public void setGmailMessageId(String gmailMessageId) {
        this.gmailMessageId = gmailMessageId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public OffsetDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(OffsetDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public boolean isHasActionableEvent() {
        return hasActionableEvent;
    }

    public void setHasActionableEvent(boolean hasActionableEvent) {
        this.hasActionableEvent = hasActionableEvent;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}
