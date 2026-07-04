CREATE TABLE gmail_allowlist_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    entry_type VARCHAR(20) NOT NULL,
    entry_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gmail_allowlist_entries_account_id FOREIGN KEY (account_id) REFERENCES oauth_accounts(id) ON DELETE CASCADE,
    CONSTRAINT uq_gmail_allowlist_entries_account_type_value UNIQUE (account_id, entry_type, entry_value),
    CONSTRAINT ck_gmail_allowlist_entries_type CHECK (entry_type IN ('SENDER', 'SUBJECT'))
);

CREATE INDEX idx_gmail_allowlist_entries_account_id ON gmail_allowlist_entries(account_id);
