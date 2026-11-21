-- Seed Tunisian Reference Data: Governorates, Delegations, Localities, Users, Vendors, Categories, Products

-- 1. Governorates (Selected key Tunisian regions)
INSERT INTO governorates (id, code, name_ar, name_fr, refusal_risk_factor) VALUES
(1, 'TUN', 'تونس', 'Tunis', 1.0),
(2, 'SFX', 'صفاقس', 'Sfax', 1.0),
(3, 'SUS', 'سوسة', 'Sousse', 1.1),
(4, 'NAB', 'نابل', 'Nabeul', 1.0),
(5, 'BIZ', 'بنزرت', 'Bizerte', 1.2),
(6, 'TOZ', 'توزر', 'Tozeur', 1.3),
(7, 'KAI', 'القيروان', 'Kairouan', 1.4),
(8, 'MED', 'مدنين', 'Medenine', 1.3);

INSERT INTO delegations (id, governorate_id, name_ar, name_fr) VALUES
(101, 1, 'تونس المدينة', 'Tunis Medina'),
(201, 2, 'صفاقس المدينة', 'Sfax Ville'),
(301, 3, 'سوسة جوهرة', 'Sousse Jawhara'),
(401, 4, 'نابل المدينة', 'Nabeul Ville'),
(601, 6, 'توزر المدينة', 'Tozeur Ville');

INSERT INTO localities (id, delegation_id, name_ar, name_fr, postal_code) VALUES
(1001, 101, 'باب سويقة', 'Bab Souika', '1006'),
(2001, 201, 'الناصرية', 'Ennasria', '3000'),
(3001, 301, 'خزامة', 'Khezama', '4051'),
(4001, 401, 'سيدي عاشور', 'Sidi Achour', '8000'),
(6001, 601, 'البلدية', 'Tozeur Centre', '2200');

-- 2. Users (Buyer, Vendor, Admin)
INSERT INTO users (id, phone, full_name, email, role) VALUES
(1, '+21620111222', 'Foued Ben Salem', 'foued.buyer@hanout.tn', 'BUYER'),
(2, '+21698333444', 'Haj Taieb Sfaxi', 'taieb.vendor@hanout.tn', 'VENDOR'),
(3, '+21650999888', 'Admin Hanout', 'admin@hanout.tn', 'ADMIN'),
(4, '+21622555666', 'Mabrouk Jerbi', 'mabrouk.vendor@hanout.tn', 'VENDOR');

-- 3. Vendors
INSERT INTO vendors (id, user_id, display_name, vat_registered, tax_id, governorate_id, status) VALUES
(1, 2, 'Huilerie Du Sud Sfax', TRUE, '1234567/A/M/000', 2, 'ACTIVE'),
(2, 4, 'Artisanat & Terroir Djerba', FALSE, '9876543/B/P/000', 8, 'ACTIVE');

-- 4. Categories with Tunisian VAT rates
INSERT INTO categories (id, slug, name_ar, name_fr, name_en, vat_rate) VALUES
(1, 'food-terroir', 'منتوجات فلاحية وتغذية', 'Produits du terroir', 'Food & Agriculture', 7.0),
(2, 'handicraft', 'صناعات تقليدية', 'Artisanat tunisien', 'Handicrafts', 19.0),
(3, 'cosmetics-wellness', 'زيوت طبيعية وعناية', 'Cosmétique naturelle', 'Natural Cosmetics', 19.0);

-- 5. Products, Translations, Aliases, SKUs and Haggling Policies

-- Product 1: Sfax Extra Virgin Olive Oil (5 Liters)
INSERT INTO products (id, vendor_id, category_id, slug, status) VALUES
(1, 1, 1, 'sfax-extra-virgin-olive-oil-5l', 'ACTIVE');

INSERT INTO product_i18n (id, product_id, locale, name, description) VALUES
(1, 1, 'ar', 'زيت زيتون بكر ممتاز صفاقس 5 لتر', 'زيت زيتون طبيعي بكر ممتاز معصور على البارد من حقول صفاقس الغناء'),
(2, 1, 'fr', 'Huile d''olive extra vierge de Sfax 5L', 'Huile d''olive de première pression à froid, terroir de Sfax'),
(3, 1, 'en', 'Sfax Extra Virgin Olive Oil 5L', 'Cold-pressed extra virgin olive oil from sunny Sfax groves');

INSERT INTO product_aliases (id, product_id, alias, kind) VALUES
(1, 1, 'zitoun', 'arabizi'),
(2, 1, 'zit zitoune', 'synonym'),
(3, 1, 'zeyt', 'arabizi'),
(4, 1, 'sfax oil', 'synonym');

INSERT INTO skus (id, product_id, sku_code, price_millimes, stock_on_hand, reserved, version) VALUES
(1, 1, 'SKU-ZIT-SFAX-5L', 125000, 50, 0, 0); -- 125.000 TND

INSERT INTO haggling_policies (id, vendor_id, product_id, auto_accept_pct, auto_reject_pct, max_rounds, offer_ttl_hours, price_lock_minutes, floor_millimes) VALUES
(1, 1, 1, 95.0, 70.0, 3, 24, 30, 100000); -- Floor: 100.000 TND

-- Product 2: Traditional Nabeul Harissa (1kg artisanal jar)
INSERT INTO products (id, vendor_id, category_id, slug, status) VALUES
(2, 1, 1, 'harissa-arbi-nabeul-1kg', 'ACTIVE');

INSERT INTO product_i18n (id, product_id, locale, name, description) VALUES
(4, 2, 'ar', 'هريسة عربي أصيلة نابل 1 كغ', 'هريسة دياري محضرة بالطرق التقليدية بفلفل الوطن القبلي المجفف شمسيا'),
(5, 2, 'fr', 'Harissa arbi artisanale de Nabeul 1kg', 'Harissa traditionnelle préparée avec des piments séchés au soleil du Cap Bon'),
(6, 2, 'en', 'Authentic Nabeul Artisanal Harissa 1kg', 'Sun-dried red peppers with coriander, garlic and caraway');

INSERT INTO product_aliases (id, product_id, alias, kind) VALUES
(5, 2, '7rissa', 'arabizi'),
(6, 2, 'hrisa', 'arabizi'),
(7, 2, 'harisa diari', 'synonym');

INSERT INTO skus (id, product_id, sku_code, price_millimes, stock_on_hand, reserved, version) VALUES
(2, 2, 'SKU-HRS-NAB-1KG', 18500, 100, 0, 0); -- 18.500 TND

INSERT INTO haggling_policies (id, vendor_id, product_id, auto_accept_pct, auto_reject_pct, max_rounds, offer_ttl_hours, price_lock_minutes, floor_millimes) VALUES
(2, 1, 2, 90.0, 75.0, 3, 24, 30, 15000);

-- Product 3: Premium Deglet Nour Dates from Tozeur (2kg box)
INSERT INTO products (id, vendor_id, category_id, slug, status) VALUES
(3, 1, 1, 'tozeur-deglet-nour-dates-2kg', 'ACTIVE');

INSERT INTO product_i18n (id, product_id, locale, name, description) VALUES
(7, 3, 'ar', 'دقلة النور توزر فرز أول 2 كغ', 'تمر دقلة النور الذهبي الفاخر من واحات الجريد بتوزر'),
(8, 3, 'fr', 'Dattes Deglet Nour de Tozeur 2kg', 'Dattes branchées dorées et translucides des oasis du Jérid'),
(9, 3, 'en', 'Tozeur Deglet Nour Premium Dates 2kg', 'Translucent amber golden dates directly from Tozeur oasis');

INSERT INTO product_aliases (id, product_id, alias, kind) VALUES
(8, 3, 'degla', 'arabizi'),
(9, 3, 'tmar', 'arabizi'),
(10, 3, 'dattes tozeur', 'synonym');

INSERT INTO skus (id, product_id, sku_code, price_millimes, stock_on_hand, reserved, version) VALUES
(3, 3, 'SKU-DGL-TOZ-2KG', 28000, 40, 0, 0); -- 28.000 TND

INSERT INTO haggling_policies (id, vendor_id, product_id, auto_accept_pct, auto_reject_pct, max_rounds, offer_ttl_hours, price_lock_minutes, floor_millimes) VALUES
(3, 1, 3, 92.0, 75.0, 3, 24, 30, 22000);

-- Product 4: Handcrafted Guellala Pottery Tagine from Djerba
INSERT INTO products (id, vendor_id, category_id, slug, status) VALUES
(4, 2, 2, 'djerba-guellala-tagine-artisanat', 'ACTIVE');

INSERT INTO product_i18n (id, product_id, locale, name, description) VALUES
(10, 4, 'ar', 'طاجين فخار قلالة جربة تقليدي', 'فخار أصيل مصنوع يدويا من طين قرية قلالة العريقة بجزيرة جربة'),
(11, 4, 'fr', 'Plat à tajine en terre cuite de Guellala Djerba', 'Poterie traditionnelle façonnée à la main à Guellala'),
(12, 4, 'en', 'Handcrafted Terracotta Tagine from Guellala Djerba', 'Authentic handmade pottery tagine from the historic village of Guellala');

INSERT INTO product_aliases (id, product_id, alias, kind) VALUES
(11, 4, 'fajjar', 'arabizi'),
(12, 4, 'fokhar', 'arabizi'),
(13, 4, 'djerba pottery', 'synonym');

INSERT INTO skus (id, product_id, sku_code, price_millimes, stock_on_hand, reserved, version) VALUES
(4, 4, 'SKU-POT-JER-001', 45000, 15, 0, 0); -- 45.000 TND

-- Vendor 2 default policy
INSERT INTO haggling_policies (id, vendor_id, product_id, auto_accept_pct, auto_reject_pct, max_rounds, offer_ttl_hours, price_lock_minutes, floor_millimes) VALUES
(4, 2, 4, 90.0, 70.0, 3, 24, 30, 35000);
