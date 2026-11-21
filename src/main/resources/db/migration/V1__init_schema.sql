-- Core schema for Hanout modular monolith

-- 1. Identity & Geography
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(32) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    role VARCHAR(32) NOT NULL,
    otp_code VARCHAR(16),
    otp_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE governorates (
    id BIGINT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name_ar VARCHAR(100) NOT NULL,
    name_fr VARCHAR(100) NOT NULL,
    refusal_risk_factor NUMERIC(4, 2) NOT NULL DEFAULT 1.0
);

CREATE TABLE delegations (
    id BIGINT PRIMARY KEY,
    governorate_id BIGINT NOT NULL REFERENCES governorates(id),
    name_ar VARCHAR(100) NOT NULL,
    name_fr VARCHAR(100) NOT NULL
);

CREATE TABLE localities (
    id BIGINT PRIMARY KEY,
    delegation_id BIGINT NOT NULL REFERENCES delegations(id),
    name_ar VARCHAR(100) NOT NULL,
    name_fr VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10) NOT NULL
);

-- 2. Vendors & Policies
CREATE TABLE vendors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    display_name VARCHAR(255) NOT NULL,
    vat_registered BOOLEAN NOT NULL DEFAULT FALSE,
    tax_id VARCHAR(64),
    governorate_id BIGINT REFERENCES governorates(id),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE haggling_policies (
    id BIGSERIAL PRIMARY KEY,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    product_id BIGINT,
    auto_accept_pct NUMERIC(5, 2) NOT NULL DEFAULT 95.0,
    auto_reject_pct NUMERIC(5, 2) NOT NULL DEFAULT 70.0,
    max_rounds INT NOT NULL DEFAULT 3,
    offer_ttl_hours INT NOT NULL DEFAULT 24,
    price_lock_minutes INT NOT NULL DEFAULT 30,
    floor_millimes BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_vendor_product UNIQUE (vendor_id, product_id)
);

-- 3. Catalog
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(100) NOT NULL UNIQUE,
    name_ar VARCHAR(100) NOT NULL,
    name_fr VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    vat_rate NUMERIC(5, 2) NOT NULL DEFAULT 19.0
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    slug VARCHAR(150) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_i18n (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    locale VARCHAR(10) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    CONSTRAINT uk_product_locale UNIQUE (product_id, locale)
);

CREATE TABLE product_aliases (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    alias VARCHAR(255) NOT NULL,
    kind VARCHAR(32) NOT NULL -- 'arabizi', 'synonym', 'typo'
);

CREATE TABLE skus (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    sku_code VARCHAR(64) NOT NULL UNIQUE,
    price_millimes BIGINT NOT NULL,
    stock_on_hand INT NOT NULL DEFAULT 0,
    reserved INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0
);

-- 4. Inventory Reservations
CREATE TABLE stock_reservations (
    id BIGSERIAL PRIMARY KEY,
    sku_id BIGINT NOT NULL REFERENCES skus(id),
    qty INT NOT NULL,
    reason VARCHAR(32) NOT NULL, -- 'OFFER', 'CART', 'ORDER'
    ref_id VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. Haggling Engine (Offers & Events)
CREATE TABLE offers (
    id BIGSERIAL PRIMARY KEY,
    sku_id BIGINT NOT NULL REFERENCES skus(id),
    buyer_id BIGINT NOT NULL REFERENCES users(id),
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    qty INT NOT NULL DEFAULT 1,
    list_price BIGINT NOT NULL,
    current_amount BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    round INT NOT NULL DEFAULT 1,
    expires_at TIMESTAMP NOT NULL,
    lock_expires_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE offer_events (
    id BIGSERIAL PRIMARY KEY,
    offer_id BIGINT NOT NULL REFERENCES offers(id) ON DELETE CASCADE,
    actor VARCHAR(32) NOT NULL, -- 'BUYER', 'VENDOR', 'SYSTEM'
    type VARCHAR(32) NOT NULL,  -- 'SUBMITTED', 'COUNTERED', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'WITHDRAWN'
    amount BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. Cart
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    buyer_id BIGINT NOT NULL REFERENCES users(id),
    sku_id BIGINT NOT NULL REFERENCES skus(id),
    qty INT NOT NULL DEFAULT 1,
    offer_id BIGINT REFERENCES offers(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_buyer_sku UNIQUE (buyer_id, sku_id)
);

-- 7. Ordering (Parent & Vendor Sub-orders)
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    buyer_id BIGINT NOT NULL REFERENCES users(id),
    total_amount BIGINT NOT NULL,
    cod_amount BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    shipping_address TEXT NOT NULL,
    phone VARCHAR(32) NOT NULL,
    governorate_id BIGINT REFERENCES governorates(id),
    idempotency_key VARCHAR(128) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vendor_orders (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    sub_total BIGINT NOT NULL,
    commission_amount BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    courier_id VARCHAR(64),
    tracking_no VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_lines (
    id BIGSERIAL PRIMARY KEY,
    vendor_order_id BIGINT NOT NULL REFERENCES vendor_orders(id) ON DELETE CASCADE,
    sku_id BIGINT NOT NULL REFERENCES skus(id),
    offer_id BIGINT REFERENCES offers(id),
    qty INT NOT NULL,
    unit_price_millimes BIGINT NOT NULL, -- snapshot!
    vat_rate NUMERIC(5, 2) NOT NULL,
    total_millimes BIGINT NOT NULL
);

-- 8. Payments & Shipments
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    gateway VARCHAR(32) NOT NULL, -- 'COD', 'KONNECT', 'FLOUCI'
    external_ref VARCHAR(128),
    amount BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    idempotency_key VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shipments (
    id BIGSERIAL PRIMARY KEY,
    vendor_order_id BIGINT NOT NULL REFERENCES vendor_orders(id),
    courier_code VARCHAR(32) NOT NULL,
    tracking_no VARCHAR(128) NOT NULL,
    cod_amount BIGINT NOT NULL DEFAULT 0,
    cod_collected BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 9. Invoices & Fiscal Stamp
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    vendor_order_id BIGINT NOT NULL REFERENCES vendor_orders(id),
    invoice_number VARCHAR(64) NOT NULL UNIQUE,
    sequence_number BIGINT NOT NULL,
    year INT NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_address TEXT NOT NULL,
    customer_tax_id VARCHAR(64),
    supplier_tax_id VARCHAR(64) NOT NULL,
    subtotal_ht BIGINT NOT NULL,
    vat_amount BIGINT NOT NULL,
    fiscal_stamp BIGINT NOT NULL DEFAULT 1000,
    total_ttc BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ISSUED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_vendor_seq_year UNIQUE (vendor_id, year, sequence_number)
);

CREATE TABLE invoice_lines (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    qty INT NOT NULL,
    unit_price_ht BIGINT NOT NULL,
    vat_rate NUMERIC(5, 2) NOT NULL,
    vat_amount BIGINT NOT NULL,
    total_ttc BIGINT NOT NULL
);

-- 10. Financial Ledger
CREATE TABLE ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    account VARCHAR(64) NOT NULL, -- 'PLATFORM_COMMISSION', 'VENDOR_PAYABLE', 'COD_RECEIVABLE_FROM_COURIER', etc.
    amount BIGINT NOT NULL,
    direction VARCHAR(16) NOT NULL, -- 'DEBIT', 'CREDIT'
    ref_type VARCHAR(64) NOT NULL,
    ref_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 11. Risk Assessments
CREATE TABLE risk_assessments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    score INT NOT NULL,
    signals TEXT NOT NULL,
    decision VARCHAR(32) NOT NULL, -- 'ALLOW', 'REQUIRE_OTP', 'REQUIRE_DEPOSIT', 'BLOCK'
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance & sweeping
CREATE INDEX idx_offers_status_expires ON offers(status, expires_at);
CREATE INDEX idx_offers_lock_expires ON offers(status, lock_expires_at);
CREATE INDEX idx_reservations_expires ON stock_reservations(status, expires_at);
CREATE INDEX idx_product_i18n_locale ON product_i18n(locale, name);
CREATE INDEX idx_product_aliases_alias ON product_aliases(alias);
CREATE INDEX idx_ledger_ref ON ledger_entries(ref_type, ref_id);
