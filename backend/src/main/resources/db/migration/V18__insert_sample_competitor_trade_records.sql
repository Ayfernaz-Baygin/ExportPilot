-- Synthetic development data for testing Competitive Accessibility.
-- These records are not official trade statistics.
--
-- Only the analysis end year (2025) is required by
-- CompetitiveAccessibilityCalculator.
--
-- Existing Türkiye supplier records are preserved. The competitor records
-- below complete the sample supplier market for Poland, Germany and Romania.

INSERT INTO trade_records (
    source,
    reporter_country_id,
    partner_scope,
    partner_country_id,
    product_code_id,
    trade_flow,
    trade_year,
    trade_value_usd,
    quantity,
    quantity_unit,
    net_weight_kg,
    source_record_id,
    source_record_date,
    revision_status,
    data_status
)
VALUES

-- -------------------------------------------------------------------------
-- Poland: 2025 total market = 465,000,000 USD
-- Existing Türkiye record = 13,900,000 USD
-- Added competitors = Germany and Romania
-- -------------------------------------------------------------------------
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2025,
    260000000.00,
    65000.000,
    'TON',
    65000000.000,
    'SAMPLE-PL-DE-150910200000-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2025,
    191100000.00,
    54600.000,
    'TON',
    54600000.000,
    'SAMPLE-PL-RO-150910200000-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),

-- -------------------------------------------------------------------------
-- Germany: 2025 total market = 780,000,000 USD
-- Existing Türkiye record = 19,200,000 USD
-- Added competitors = Poland and Romania
-- -------------------------------------------------------------------------
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2025,
    410000000.00,
    102500.000,
    'TON',
    102500000.000,
    'SAMPLE-DE-PL-150910200000-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2025,
    350800000.00,
    97444.444,
    'TON',
    97444444.000,
    'SAMPLE-DE-RO-150910200000-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),

-- -------------------------------------------------------------------------
-- Romania: 2025 total market = 300,000,000 USD
-- Existing Türkiye record = 9,500,000 USD
-- Added competitors = Germany and Poland
-- -------------------------------------------------------------------------
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2025,
    180000000.00,
    45000.000,
    'TON',
    45000000.000,
    'SAMPLE-RO-DE-150910200000-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2025,
    110500000.00,
    32500.000,
    'TON',
    32500000.000,
    'SAMPLE-RO-PL-150910200000-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
);