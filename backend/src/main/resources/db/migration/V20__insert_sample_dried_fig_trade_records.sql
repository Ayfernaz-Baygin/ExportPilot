-- Synthetic development data for dried fig market analyses.
-- These records are created only for application development and testing.
-- They must not be interpreted as official trade statistics.
--
-- Product:
-- HS 080420 - Dried figs
--
-- Reporter markets:
-- Germany, Poland and Romania
--
-- Data coverage:
-- 2021-2025 WORLD_TOTAL import records
-- 2021-2025 Türkiye supplier records
-- 2025 competitor supplier records

-- ============================================================
-- 1. WORLD TOTAL IMPORT RECORDS
-- ============================================================

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

-- ------------------------------------------------------------
-- Germany: total dried fig imports from the world
-- ------------------------------------------------------------

(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2021,
    210000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-DE-080420-2021',
    DATE '2021-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2022,
    225000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-DE-080420-2022',
    DATE '2022-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2023,
    245000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-DE-080420-2023',
    DATE '2023-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2024,
    270000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-DE-080420-2024',
    DATE '2024-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2025,
    300000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-DE-080420-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),

-- ------------------------------------------------------------
-- Poland: total dried fig imports from the world
-- ------------------------------------------------------------

(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2021,
    105000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-PL-080420-2021',
    DATE '2021-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2022,
    120000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-PL-080420-2022',
    DATE '2022-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2023,
    138000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-PL-080420-2023',
    DATE '2023-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2024,
    157000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-PL-080420-2024',
    DATE '2024-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2025,
    180000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-PL-080420-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),

-- ------------------------------------------------------------
-- Romania: total dried fig imports from the world
-- ------------------------------------------------------------

(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2021,
    62000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-RO-080420-2021',
    DATE '2021-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2022,
    73000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-RO-080420-2022',
    DATE '2022-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2023,
    87000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-RO-080420-2023',
    DATE '2023-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2024,
    104000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-RO-080420-2024',
    DATE '2024-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2025,
    125000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-RO-080420-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
);


-- ============================================================
-- 2. IMPORTS FROM TÜRKİYE
-- ============================================================

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

-- ------------------------------------------------------------
-- Germany's dried fig imports from Türkiye
-- ------------------------------------------------------------

(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2021,
    78000000.00,
    19500.000,
    'TON',
    19500000.000,
    'SAMPLE-DE-TR-080420-2021',
    DATE '2021-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2022,
    84000000.00,
    20500.000,
    'TON',
    20500000.000,
    'SAMPLE-DE-TR-080420-2022',
    DATE '2022-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2023,
    92000000.00,
    21800.000,
    'TON',
    21800000.000,
    'SAMPLE-DE-TR-080420-2023',
    DATE '2023-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2024,
    103000000.00,
    23500.000,
    'TON',
    23500000.000,
    'SAMPLE-DE-TR-080420-2024',
    DATE '2024-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2025,
    115000000.00,
    25500.000,
    'TON',
    25500000.000,
    'SAMPLE-DE-TR-080420-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),

-- ------------------------------------------------------------
-- Poland's dried fig imports from Türkiye
-- ------------------------------------------------------------

(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2021,
    36000000.00,
    9000.000,
    'TON',
    9000000.000,
    'SAMPLE-PL-TR-080420-2021',
    DATE '2021-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2022,
    42000000.00,
    10200.000,
    'TON',
    10200000.000,
    'SAMPLE-PL-TR-080420-2022',
    DATE '2022-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2023,
    50000000.00,
    11800.000,
    'TON',
    11800000.000,
    'SAMPLE-PL-TR-080420-2023',
    DATE '2023-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2024,
    59000000.00,
    13400.000,
    'TON',
    13400000.000,
    'SAMPLE-PL-TR-080420-2024',
    DATE '2024-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2025,
    70000000.00,
    15500.000,
    'TON',
    15500000.000,
    'SAMPLE-PL-TR-080420-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),

-- ------------------------------------------------------------
-- Romania's dried fig imports from Türkiye
-- ------------------------------------------------------------

(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2021,
    22000000.00,
    5500.000,
    'TON',
    5500000.000,
    'SAMPLE-RO-TR-080420-2021',
    DATE '2021-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2022,
    27000000.00,
    6500.000,
    'TON',
    6500000.000,
    'SAMPLE-RO-TR-080420-2022',
    DATE '2022-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2023,
    34000000.00,
    8000.000,
    'TON',
    8000000.000,
    'SAMPLE-RO-TR-080420-2023',
    DATE '2023-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2024,
    42000000.00,
    9500.000,
    'TON',
    9500000.000,
    'SAMPLE-RO-TR-080420-2024',
    DATE '2024-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2025,
    52000000.00,
    11500.000,
    'TON',
    11500000.000,
    'SAMPLE-RO-TR-080420-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
);


-- ============================================================
-- 3. COMPETITOR SUPPLIER RECORDS FOR 2025
-- ============================================================
--
-- These records complete the synthetic supplier market used by
-- CompetitiveAccessibilityCalculator.
--
-- Germany:
-- Total market = 300,000,000
-- Türkiye      = 115,000,000
-- Poland       = 110,000,000
-- Romania      = 75,000,000
--
-- Poland:
-- Total market = 180,000,000
-- Türkiye      = 70,000,000
-- Germany      = 65,000,000
-- Romania      = 45,000,000
--
-- Romania:
-- Total market = 125,000,000
-- Türkiye      = 52,000,000
-- Germany      = 43,000,000
-- Poland       = 30,000,000

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

-- Germany's competitor suppliers

(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2025,
    110000000.00,
    24000.000,
    'TON',
    24000000.000,
    'SAMPLE-DE-PL-080420-2025',
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
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2025,
    75000000.00,
    16500.000,
    'TON',
    16500000.000,
    'SAMPLE-DE-RO-080420-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),

-- Poland's competitor suppliers

(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2025,
    65000000.00,
    14200.000,
    'TON',
    14200000.000,
    'SAMPLE-PL-DE-080420-2025',
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
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2025,
    45000000.00,
    9800.000,
    'TON',
    9800000.000,
    'SAMPLE-PL-RO-080420-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),

-- Romania's competitor suppliers

(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    'SPECIFIC_COUNTRY',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2025,
    43000000.00,
    9500.000,
    'TON',
    9500000.000,
    'SAMPLE-RO-DE-080420-2025',
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
        WHERE code = '080420'
          AND code_type = 'HS'
    ),
    'IMPORT',
    2025,
    30000000.00,
    6700.000,
    'TON',
    6700000.000,
    'SAMPLE-RO-PL-080420-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
);