-- Synthetic development data.
-- These records represent each reporter country's total imports
-- from the world and must not be interpreted as official statistics.

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

-- Germany: total olive oil imports from the world
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2021,
    610000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-DE-150910200000-2021',
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
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2022,
    645000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-DE-150910200000-2022',
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
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2023,
    690000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-DE-150910200000-2023',
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
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2024,
    735000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-DE-150910200000-2024',
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
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2025,
    780000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-DE-150910200000-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),

-- Poland: total olive oil imports from the world
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2021,
    280000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-PL-150910200000-2021',
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
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2022,
    315000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-PL-150910200000-2022',
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
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2023,
    355000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-PL-150910200000-2023',
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
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2024,
    405000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-PL-150910200000-2024',
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
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2025,
    465000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-PL-150910200000-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),

-- Romania: total olive oil imports from the world
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    'WORLD_TOTAL',
    NULL,
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2021,
    165000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-RO-150910200000-2021',
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
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2022,
    190000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-RO-150910200000-2022',
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
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2023,
    220000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-RO-150910200000-2023',
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
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2024,
    255000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-RO-150910200000-2024',
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
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2025,
    300000000.00,
    NULL,
    NULL,
    NULL,
    'SAMPLE-WORLD-RO-150910200000-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
);