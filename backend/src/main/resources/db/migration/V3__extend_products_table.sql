ALTER TABLE products
    ADD COLUMN sub_category VARCHAR(100),
    ADD COLUMN unit VARCHAR(50),
    ADD COLUMN description VARCHAR(1000),
    ADD COLUMN scientific_name VARCHAR(200);

CREATE INDEX idx_products_name
    ON products(name);

CREATE INDEX idx_products_category
    ON products(category);

CREATE INDEX idx_products_sector
    ON products(sector);