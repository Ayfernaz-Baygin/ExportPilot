ALTER TABLE countries
    ALTER COLUMN iso2_code TYPE VARCHAR(2)
    USING TRIM(iso2_code);

ALTER TABLE countries
    ALTER COLUMN iso3_code TYPE VARCHAR(3)
    USING TRIM(iso3_code);