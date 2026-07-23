CREATE TABLE workflow_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    triggered_by_user_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'RUNNING',
    current_step VARCHAR(100),
    emails_processed INTEGER NOT NULL DEFAULT 0,
    events_extracted INTEGER NOT NULL DEFAULT 0,
    conflicts_detected INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_workflow_executions_user_status ON workflow_executions(triggered_by_user_id, status);
CREATE INDEX idx_workflow_executions_started_at ON workflow_executions(started_at DESC);
