CREATE TABLE trade_records
(
    id BIGSERIAL PRIMARY KEY,

    source VARCHAR(50) NOT NULL,

    reporter_country_id BIGINT NOT NULL,
    partner_country_id BIGINT NOT NULL,
    product_code_id BIGINT NOT NULL,

    trade_flow VARCHAR(10) NOT NULL,
    trade_year INTEGER NOT NULL,

    trade_value_usd NUMERIC(20, 2),
    quantity NUMERIC(20, 3),
    quantity_unit VARCHAR(30),
    net_weight_kg NUMERIC(20, 3),

    source_record_id VARCHAR(150),
    source_record_date DATE,

    revision_status VARCHAR(30) NOT NULL DEFAULT 'ORIGINAL',
    data_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',

    retrieved_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_trade_records_reporter_country
        FOREIGN KEY (reporter_country_id)
        REFERENCES countries(id),

    CONSTRAINT fk_trade_records_partner_country
        FOREIGN KEY (partner_country_id)
        REFERENCES countries(id),

    CONSTRAINT fk_trade_records_product_code
        FOREIGN KEY (product_code_id)
        REFERENCES product_codes(id),

    CONSTRAINT chk_trade_records_flow
        CHECK (trade_flow IN ('IMPORT', 'EXPORT')),

    CONSTRAINT chk_trade_records_year
        CHECK (trade_year BETWEEN 1990 AND 2100),

    CONSTRAINT chk_trade_records_value
        CHECK (
            trade_value_usd IS NULL
            OR trade_value_usd >= 0
        ),

    CONSTRAINT chk_trade_records_quantity
        CHECK (
            quantity IS NULL
            OR quantity >= 0
        ),

    CONSTRAINT chk_trade_records_net_weight
        CHECK (
            net_weight_kg IS NULL
            OR net_weight_kg >= 0
        ),

    CONSTRAINT chk_trade_records_revision_status
        CHECK (
            revision_status IN (
                'ORIGINAL',
                'REVISED',
                'FINAL'
            )
        ),

    CONSTRAINT chk_trade_records_data_status
        CHECK (
            data_status IN (
                'AVAILABLE',
                'PARTIAL',
                'MISSING',
                'ESTIMATED'
            )
        ),

    CONSTRAINT uq_trade_records_dimensions
        UNIQUE (
            source,
            reporter_country_id,
            partner_country_id,
            product_code_id,
            trade_flow,
            trade_year
        )
);

CREATE INDEX idx_trade_records_reporter_country
    ON trade_records(reporter_country_id);

CREATE INDEX idx_trade_records_partner_country
    ON trade_records(partner_country_id);

CREATE INDEX idx_trade_records_product_code
    ON trade_records(product_code_id);

CREATE INDEX idx_trade_records_year
    ON trade_records(trade_year);

CREATE INDEX idx_trade_records_flow
    ON trade_records(trade_flow);

CREATE INDEX idx_trade_records_analysis_lookup
    ON trade_records(
        product_code_id,
        reporter_country_id,
        trade_flow,
        trade_year
    );