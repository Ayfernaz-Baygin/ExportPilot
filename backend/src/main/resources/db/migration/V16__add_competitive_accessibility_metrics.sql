ALTER TABLE analysis_country_results
    ADD COLUMN supplier_count INTEGER,
    ADD COLUMN supplier_concentration_hhi NUMERIC(12, 2),
    ADD COLUMN turkey_supplier_rank INTEGER,
    ADD COLUMN turkey_market_share_percent NUMERIC(12, 4),
    ADD COLUMN leader_market_share_percent NUMERIC(12, 4),
    ADD COLUMN distance_to_leader_percent NUMERIC(12, 4),
    ADD COLUMN turkey_unit_value_usd_per_kg NUMERIC(20, 4),
    ADD COLUMN market_average_unit_value_usd_per_kg NUMERIC(20, 4);