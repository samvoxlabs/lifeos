CREATE TABLE mail_sync_states (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL UNIQUE,
    last_history_id VARCHAR(255),
    last_synced_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mail_sync_states_account_id FOREIGN KEY (account_id) REFERENCES oauth_accounts(id) ON DELETE CASCADE
);

CREATE INDEX idx_mail_sync_states_account_id ON mail_sync_states(account_id);

CREATE TABLE mail_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    gmail_message_id VARCHAR(255) NOT NULL,
    thread_id VARCHAR(255),
    sender VARCHAR(512),
    subject VARCHAR(1000),
    snippet TEXT,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    history_id VARCHAR(255),
    has_actionable_event BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mail_messages_account_id FOREIGN KEY (account_id) REFERENCES oauth_accounts(id) ON DELETE CASCADE,
    CONSTRAINT uq_mail_messages_account_gmail_message UNIQUE (account_id, gmail_message_id)
);

CREATE INDEX idx_mail_messages_account_received_at ON mail_messages(account_id, received_at DESC);

CREATE TABLE mail_extracted_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mail_message_id UUID NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    timezone_offset VARCHAR(10) NOT NULL,
    assigned_to_email VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mail_extracted_events_mail_message_id FOREIGN KEY (mail_message_id) REFERENCES mail_messages(id) ON DELETE CASCADE
);

CREATE INDEX idx_mail_extracted_events_starts_ends ON mail_extracted_events(starts_at, ends_at);

CREATE TABLE mail_conflicts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    extracted_event_id UUID NOT NULL,
    conflicting_event_id VARCHAR(255) NOT NULL,
    conflicting_event_source VARCHAR(50) NOT NULL,
    overlap_start TIMESTAMP WITH TIME ZONE NOT NULL,
    overlap_end TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL,
    suggested_resolutions_json JSONB NOT NULL,
    applied_resolution_key VARCHAR(100),
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mail_conflicts_extracted_event_id FOREIGN KEY (extracted_event_id) REFERENCES mail_extracted_events(id) ON DELETE CASCADE,
    CONSTRAINT uq_mail_conflicts_event_target UNIQUE (extracted_event_id, conflicting_event_id, conflicting_event_source)
);

CREATE INDEX idx_mail_conflicts_status ON mail_conflicts(status);

CREATE TABLE mail_resolution_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conflict_id UUID NOT NULL,
    action_key VARCHAR(100) NOT NULL,
    calendar_updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    notification_sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mail_resolution_executions_conflict_id FOREIGN KEY (conflict_id) REFERENCES mail_conflicts(id) ON DELETE CASCADE,
    CONSTRAINT uq_mail_resolution_executions_conflict_action UNIQUE (conflict_id, action_key)
);

CREATE INDEX idx_mail_resolution_executions_conflict ON mail_resolution_executions(conflict_id);
