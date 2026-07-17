-- These records are synthetic development data.
-- They must not be interpreted as official trade statistics.

INSERT INTO trade_records (
    source,
    reporter_country_id,
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

-- Poland's olive oil imports from Türkiye
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2021,
    7200000.00,
    2400.000,
    'TON',
    2400000.000,
    'SAMPLE-PL-TR-150910200000-2021',
    DATE '2021-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2022,
    8450000.00,
    2700.000,
    'TON',
    2700000.000,
    'SAMPLE-PL-TR-150910200000-2022',
    DATE '2022-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2023,
    9900000.00,
    3000.000,
    'TON',
    3000000.000,
    'SAMPLE-PL-TR-150910200000-2023',
    DATE '2023-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2024,
    11800000.00,
    3350.000,
    'TON',
    3350000.000,
    'SAMPLE-PL-TR-150910200000-2024',
    DATE '2024-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'PL'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2025,
    13900000.00,
    3700.000,
    'TON',
    3700000.000,
    'SAMPLE-PL-TR-150910200000-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),

-- Germany's olive oil imports from Türkiye
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2021,
    15100000.00,
    4800.000,
    'TON',
    4800000.000,
    'SAMPLE-DE-TR-150910200000-2021',
    DATE '2021-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2022,
    16000000.00,
    4950.000,
    'TON',
    4950000.000,
    'SAMPLE-DE-TR-150910200000-2022',
    DATE '2022-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2023,
    17400000.00,
    5150.000,
    'TON',
    5150000.000,
    'SAMPLE-DE-TR-150910200000-2023',
    DATE '2023-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2024,
    18500000.00,
    5350.000,
    'TON',
    5350000.000,
    'SAMPLE-DE-TR-150910200000-2024',
    DATE '2024-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'DE'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2025,
    19200000.00,
    5450.000,
    'TON',
    5450000.000,
    'SAMPLE-DE-TR-150910200000-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
),

-- Romania's olive oil imports from Türkiye
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2021,
    4600000.00,
    1550.000,
    'TON',
    1550000.000,
    'SAMPLE-RO-TR-150910200000-2021',
    DATE '2021-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2022,
    5300000.00,
    1700.000,
    'TON',
    1700000.000,
    'SAMPLE-RO-TR-150910200000-2022',
    DATE '2022-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2023,
    6500000.00,
    1950.000,
    'TON',
    1950000.000,
    'SAMPLE-RO-TR-150910200000-2023',
    DATE '2023-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2024,
    7900000.00,
    2250.000,
    'TON',
    2250000.000,
    'SAMPLE-RO-TR-150910200000-2024',
    DATE '2024-12-31',
    'FINAL',
    'AVAILABLE'
),
(
    'SAMPLE',
    (SELECT id FROM countries WHERE iso2_code = 'RO'),
    (SELECT id FROM countries WHERE iso2_code = 'TR'),
    (
        SELECT id
        FROM product_codes
        WHERE code = '150910200000'
          AND code_type = 'GTIP'
    ),
    'IMPORT',
    2025,
    9500000.00,
    2600.000,
    'TON',
    2600000.000,
    'SAMPLE-RO-TR-150910200000-2025',
    DATE '2025-12-31',
    'ORIGINAL',
    'ESTIMATED'
);