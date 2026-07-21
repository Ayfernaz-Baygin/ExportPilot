CREATE TABLE ai_market_reports (
    id BIGSERIAL PRIMARY KEY,
    analysis_id BIGINT NOT NULL,
    model VARCHAR(100) NOT NULL,
    report TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ai_market_reports_analysis_id UNIQUE (analysis_id)
);