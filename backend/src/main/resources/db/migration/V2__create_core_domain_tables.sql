CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100),
    sector VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(12) NOT NULL,
    code_type VARCHAR(20) NOT NULL,
    description VARCHAR(500) NOT NULL,
    classification_level SMALLINT NOT NULL,
    product_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_codes_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT uq_product_code_type
        UNIQUE (code, code_type),

    CONSTRAINT chk_product_code_type
        CHECK (code_type IN ('HS', 'GTIP')),

    CONSTRAINT chk_classification_level
        CHECK (classification_level IN (2, 4, 6, 8, 10, 12))
);

CREATE TABLE countries (
    id BIGSERIAL PRIMARY KEY,
    iso2_code CHAR(2) NOT NULL UNIQUE,
    iso3_code CHAR(3) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    region VARCHAR(100),
    income_group VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE analyses (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    product_code_id BIGINT NOT NULL,
    start_year INTEGER NOT NULL,
    end_year INTEGER NOT NULL,
    target_region VARCHAR(100),
    max_countries INTEGER NOT NULL DEFAULT 20,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    scoring_model_version VARCHAR(50) NOT NULL DEFAULT 'v1',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ,

    CONSTRAINT fk_analyses_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT fk_analyses_product_code
        FOREIGN KEY (product_code_id)
        REFERENCES product_codes(id),

    CONSTRAINT chk_analysis_years
        CHECK (start_year <= end_year),

    CONSTRAINT chk_analysis_max_countries
        CHECK (max_countries BETWEEN 1 AND 100),

    CONSTRAINT chk_analysis_status
        CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED'))
);

CREATE TABLE analysis_country_results (
    id BIGSERIAL PRIMARY KEY,
    analysis_id BIGINT NOT NULL,
    country_id BIGINT NOT NULL,
    rank_position INTEGER,
    overall_score NUMERIC(5,2),
    import_market_size_score NUMERIC(5,2),
    import_growth_score NUMERIC(5,2),
    turkey_export_performance_score NUMERIC(5,2),
    market_share_opportunity_score NUMERIC(5,2),
    competitive_accessibility_score NUMERIC(5,2),
    macroeconomic_stability_score NUMERIC(5,2),
    currency_stability_score NUMERIC(5,2),
    logistics_suitability_score NUMERIC(5,2),
    tariff_suitability_score NUMERIC(5,2),
    data_completeness NUMERIC(5,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_analysis_country_results_analysis
        FOREIGN KEY (analysis_id)
        REFERENCES analyses(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_analysis_country_results_country
        FOREIGN KEY (country_id)
        REFERENCES countries(id),

    CONSTRAINT uq_analysis_country
        UNIQUE (analysis_id, country_id)
);

CREATE INDEX idx_product_codes_product_id
    ON product_codes(product_id);

CREATE INDEX idx_analyses_product_id
    ON analyses(product_id);

CREATE INDEX idx_analyses_product_code_id
    ON analyses(product_code_id);

CREATE INDEX idx_analysis_country_results_analysis_id
    ON analysis_country_results(analysis_id);

CREATE INDEX idx_analysis_country_results_country_id
    ON analysis_country_results(country_id);