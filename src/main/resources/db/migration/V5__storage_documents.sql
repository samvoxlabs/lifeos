CREATE TABLE storage_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    document_group VARCHAR(40) NOT NULL,
    document_key VARCHAR(120) NOT NULL,
    content_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_storage_documents_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_storage_documents_user_group_key UNIQUE (user_id, document_group, document_key)
);

CREATE INDEX idx_storage_documents_user_group ON storage_documents(user_id, document_group);
