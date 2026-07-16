ALTER TABLE analysis_country_results
    ADD COLUMN first_year INTEGER,
    ADD COLUMN last_year INTEGER,
    ADD COLUMN available_year_count INTEGER,

    ADD COLUMN first_year_trade_value_usd NUMERIC(20, 2),
    ADD COLUMN last_year_trade_value_usd NUMERIC(20, 2),
    ADD COLUMN total_trade_value_usd NUMERIC(20, 2),
    ADD COLUMN average_trade_value_usd NUMERIC(20, 2),

    ADD COLUMN absolute_growth_usd NUMERIC(20, 2),
    ADD COLUMN growth_rate_percent NUMERIC(12, 4),
    ADD COLUMN cagr_percent NUMERIC(12, 4),

    ADD COLUMN calculated_at TIMESTAMPTZ;

ALTER TABLE analysis_country_results
    ADD CONSTRAINT chk_analysis_result_years
        CHECK (
            first_year IS NULL
            OR last_year IS NULL
            OR first_year <= last_year
        ),

    ADD CONSTRAINT chk_analysis_result_available_year_count
        CHECK (
            available_year_count IS NULL
            OR available_year_count >= 0
        ),

    ADD CONSTRAINT chk_analysis_result_first_value
        CHECK (
            first_year_trade_value_usd IS NULL
            OR first_year_trade_value_usd >= 0
        ),

    ADD CONSTRAINT chk_analysis_result_last_value
        CHECK (
            last_year_trade_value_usd IS NULL
            OR last_year_trade_value_usd >= 0
        ),

    ADD CONSTRAINT chk_analysis_result_total_value
        CHECK (
            total_trade_value_usd IS NULL
            OR total_trade_value_usd >= 0
        ),

    ADD CONSTRAINT chk_analysis_result_average_value
        CHECK (
            average_trade_value_usd IS NULL
            OR average_trade_value_usd >= 0
        );

CREATE INDEX idx_analysis_country_results_rank
    ON analysis_country_results(
        analysis_id,
        rank_position
    );

CREATE INDEX idx_analysis_country_results_score
    ON analysis_country_results(
        analysis_id,
        overall_score DESC
    );