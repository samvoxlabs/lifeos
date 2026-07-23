ALTER TABLE mail_messages ADD COLUMN sender_name VARCHAR(255);
ALTER TABLE mail_messages ADD COLUMN sender_email VARCHAR(255);
ALTER TABLE mail_messages ADD COLUMN recipients_json JSONB;
ALTER TABLE mail_messages ADD COLUMN body_text TEXT;
ALTER TABLE mail_messages ADD COLUMN is_read BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE mail_messages ADD COLUMN labels_json JSONB;

ALTER TABLE mail_extracted_events ADD COLUMN timezone VARCHAR(100);
ALTER TABLE mail_extracted_events ADD COLUMN location VARCHAR(255);
ALTER TABLE mail_extracted_events ADD COLUMN event_description TEXT;
ALTER TABLE mail_extracted_events ADD COLUMN owner_id VARCHAR(100);
ALTER TABLE mail_extracted_events ADD COLUMN confidence DOUBLE PRECISION;

ALTER TABLE mail_resolution_executions ADD COLUMN idempotency_key VARCHAR(255);
CREATE UNIQUE INDEX uq_mail_resolution_executions_idempotency_key
    ON mail_resolution_executions(idempotency_key)
    WHERE idempotency_key IS NOT NULL;
