ALTER TABLE countries
    ADD COLUMN un_m49_code INTEGER;

UPDATE countries
SET un_m49_code = 276
WHERE UPPER(iso2_code) = 'DE';

UPDATE countries
SET un_m49_code = 616
WHERE UPPER(iso2_code) = 'PL';

UPDATE countries
SET un_m49_code = 642
WHERE UPPER(iso2_code) = 'RO';

UPDATE countries
SET un_m49_code = 792
WHERE UPPER(iso2_code) = 'TR';

CREATE UNIQUE INDEX uk_countries_un_m49_code
    ON countries (un_m49_code)
    WHERE un_m49_code IS NOT NULL;