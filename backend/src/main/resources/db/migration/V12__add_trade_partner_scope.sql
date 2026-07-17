ALTER TABLE trade_records
    ADD COLUMN partner_scope VARCHAR(30);

UPDATE trade_records
SET partner_scope = 'SPECIFIC_COUNTRY'
WHERE partner_scope IS NULL;

ALTER TABLE trade_records
    ALTER COLUMN partner_scope SET NOT NULL;

ALTER TABLE trade_records
    ALTER COLUMN partner_scope
        SET DEFAULT 'SPECIFIC_COUNTRY';

ALTER TABLE trade_records
    ALTER COLUMN partner_country_id DROP NOT NULL;

ALTER TABLE trade_records
    DROP CONSTRAINT uq_trade_records_dimensions;

ALTER TABLE trade_records
    ADD CONSTRAINT chk_trade_records_partner_scope
        CHECK (
            partner_scope IN (
                'SPECIFIC_COUNTRY',
                'WORLD_TOTAL'
            )
        );

ALTER TABLE trade_records
    ADD CONSTRAINT chk_trade_records_partner_consistency
        CHECK (
            (
                partner_scope = 'SPECIFIC_COUNTRY'
                AND partner_country_id IS NOT NULL
            )
            OR
            (
                partner_scope = 'WORLD_TOTAL'
                AND partner_country_id IS NULL
            )
        );

CREATE UNIQUE INDEX uq_trade_records_specific_country
    ON trade_records (
        source,
        reporter_country_id,
        partner_country_id,
        product_code_id,
        trade_flow,
        trade_year
    )
    WHERE partner_scope = 'SPECIFIC_COUNTRY';

CREATE UNIQUE INDEX uq_trade_records_world_total
    ON trade_records (
        source,
        reporter_country_id,
        product_code_id,
        trade_flow,
        trade_year
    )
    WHERE partner_scope = 'WORLD_TOTAL';

CREATE INDEX idx_trade_records_partner_scope
    ON trade_records(partner_scope);

CREATE INDEX idx_trade_records_scoring_lookup
    ON trade_records (
        product_code_id,
        partner_scope,
        trade_flow,
        trade_year
    );