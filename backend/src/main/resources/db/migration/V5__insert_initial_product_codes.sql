INSERT INTO product_codes (
    code,
    code_type,
    description,
    classification_level,
    product_id,
    active
)
SELECT
    '1509',
    'HS',
    'Zeytinyağı ve zeytinden elde edilen fraksiyonları',
    4,
    id,
    TRUE
FROM products
WHERE name = 'Zeytinyağı';

INSERT INTO product_codes (
    code,
    code_type,
    description,
    classification_level,
    product_id,
    active
)
SELECT
    '150910',
    'HS',
    'Natürel zeytinyağı',
    6,
    id,
    TRUE
FROM products
WHERE name = 'Zeytinyağı';

INSERT INTO product_codes (
    code,
    code_type,
    description,
    classification_level,
    product_id,
    active
)
SELECT
    '150910200000',
    'GTIP',
    'Natürel birinci zeytinyağı',
    12,
    id,
    TRUE
FROM products
WHERE name = 'Zeytinyağı';

INSERT INTO product_codes (
    code,
    code_type,
    description,
    classification_level,
    product_id,
    active
)
SELECT
    '080420',
    'HS',
    'İncir; taze veya kurutulmuş',
    6,
    id,
    TRUE
FROM products
WHERE name = 'Kuru İncir';

INSERT INTO product_codes (
    code,
    code_type,
    description,
    classification_level,
    product_id,
    active
)
SELECT
    '080420900000',
    'GTIP',
    'Kurutulmuş incir',
    12,
    id,
    TRUE
FROM products
WHERE name = 'Kuru İncir';

INSERT INTO product_codes (
    code,
    code_type,
    description,
    classification_level,
    product_id,
    active
)
SELECT
    '2515',
    'HS',
    'Mermer, traverten ve diğer kalkerli süsleme taşları',
    4,
    id,
    TRUE
FROM products
WHERE name = 'Mermer';

INSERT INTO product_codes (
    code,
    code_type,
    description,
    classification_level,
    product_id,
    active
)
SELECT
    '9403',
    'HS',
    'Diğer mobilyalar ve bunların aksam ve parçaları',
    4,
    id,
    TRUE
FROM products
WHERE name = 'Mobilya';