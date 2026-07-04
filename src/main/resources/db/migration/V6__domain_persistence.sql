CREATE TABLE source_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id VARCHAR(255) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    source_type VARCHAR(100) NOT NULL,
    sender VARCHAR(255),
    subject VARCHAR(500),
    raw_content TEXT NOT NULL,
    metadata_json JSONB,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processing_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_source_documents_provider_external_source UNIQUE (provider, external_id, source_type)
);

CREATE INDEX idx_source_documents_provider_external_source ON source_documents(provider, external_id, source_type);

CREATE TABLE extractions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_document_id UUID NOT NULL UNIQUE,
    summary TEXT NOT NULL,
    confidence DOUBLE PRECISION,
    model VARCHAR(120) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    prompt_version VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_extractions_source_document_id FOREIGN KEY (source_document_id) REFERENCES source_documents(id) ON DELETE CASCADE
);

CREATE TABLE actions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    action_type VARCHAR(31) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    confidence DOUBLE PRECISION,
    source_document_id UUID NOT NULL,
    extraction_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_actions_source_document_id FOREIGN KEY (source_document_id) REFERENCES source_documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_actions_extraction_id FOREIGN KEY (extraction_id) REFERENCES extractions(id) ON DELETE CASCADE
);

CREATE INDEX idx_actions_source_document_id ON actions(source_document_id);
CREATE INDEX idx_actions_extraction_id ON actions(extraction_id);
CREATE INDEX idx_actions_action_type ON actions(action_type);
