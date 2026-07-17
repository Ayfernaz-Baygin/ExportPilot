CREATE TABLE economic_indicators
(
    id BIGSERIAL PRIMARY KEY,

    country_id BIGINT NOT NULL,

    indicator_type VARCHAR(50) NOT NULL,

    indicator_year INTEGER NOT NULL,

    indicator_value NUMERIC(24, 8),

    unit VARCHAR(100) NOT NULL,

    source VARCHAR(100) NOT NULL,

    source_indicator_code VARCHAR(50) NOT NULL,

    data_status VARCHAR(30) NOT NULL,

    source_updated_at TIMESTAMP WITH TIME ZONE,

    retrieved_at TIMESTAMP WITH TIME ZONE NOT NULL,

    is_latest BOOLEAN NOT NULL DEFAULT FALSE,

    transformation_version VARCHAR(30) NOT NULL DEFAULT 'v1',

    CONSTRAINT fk_economic_indicator_country
        FOREIGN KEY (country_id)
        REFERENCES countries (id),

    CONSTRAINT uk_economic_indicator_country_type_year_source
        UNIQUE (
            country_id,
            indicator_type,
            indicator_year,
            source
        ),

    CONSTRAINT chk_economic_indicator_year
        CHECK (
            indicator_year >= 1900
            AND indicator_year <= 2100
        ),

    CONSTRAINT chk_economic_indicator_status
        CHECK (
            data_status IN (
                'AVAILABLE',
                'PARTIAL',
                'MISSING',
                'STALE',
                'API_ERROR'
            )
        )
);

CREATE INDEX idx_economic_indicator_country
    ON economic_indicators (country_id);

CREATE INDEX idx_economic_indicator_type
    ON economic_indicators (indicator_type);

CREATE INDEX idx_economic_indicator_year
    ON economic_indicators (indicator_year);

CREATE INDEX idx_economic_indicator_country_type
    ON economic_indicators (
        country_id,
        indicator_type
    );

CREATE UNIQUE INDEX uk_economic_indicator_latest
    ON economic_indicators (
        country_id,
        indicator_type,
        source
    )
    WHERE is_latest = TRUE;