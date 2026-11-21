# Hanout (حانوت) - Tunisian E-Commerce Modular Monolith

**Hanout** is an e-commerce platform and haggling marketplace built with **Spring Modulith** and **Java 17**. It models an authentic Tunisian shopping experience with an interactive price negotiation engine, trilingual search (Arabic, French, Arabizi), double-entry payout ledger, COD risk scoring, and Tunisian fiscal compliance (gapless invoicing, fiscal stamps, and VAT regimes).

---

## 1. Architecture Overview

Built as a modular monolith where module boundaries are strictly verified at build-time using `ApplicationModules.of(HanoutApplication.class).verify()`. Modules interact via public APIs and Spring Modulith event publication (guaranteeing outbox delivery).

```
com.hanout
├── identity      # Users, JWT, OTP, Roles (BUYER, VENDOR, ADMIN), libphonenumber (TN)
├── vendor        # Profiles, tax regime (flat rate vs VAT), haggling policies
├── catalog       # Products, categories, i18n translations, aliases, SKUs
├── search        # Arabic normalization, French unaccent, Arabizi mapping (3,7,9,5), aliases
├── inventory     # Stock management, atomic guarded reservations, scheduled sweeper
├── haggling      # Offers state machine, auto-rules, price locks, Bucket4j rate limiting
├── cart          # Shopping cart with locked-price offer support
├── ordering      # Checkout saga, parent orders, vendor sub-orders, snapshot pricing
├── payment       # PaymentGateway port with COD, Konnect, and Mock adapters
├── delivery      # Courier port with RapidPost adapter and COD remittance
├── risk          # COD scoring rules, signals vector, action decisions (ALLOW/OTP/DEPOSIT/BLOCK)
├── invoicing     # Gapless per-vendor numbering, VAT breakdown (19%, 13%, 7%), fiscal stamp
├── payout        # Double-entry ledger (PLATFORM_COMMISSION, VENDOR_PAYABLE, etc.)
├── notification  # SMS & Email ports, templates (Arabic with RTL, French, English)
└── common        # Value objects (Money in millimes), JPA converter, JSON serializer
```

---

## 2. Core Features & Design Principles

### Money Value Type
- `Money(long millimes)` wraps all monetary amounts. Double and float are strictly prohibited.
- Serialized to JSON as 3-decimal string (e.g. `"12.500"` TND).
- HT is mathematically derived from TTC based on category VAT rate.

### Haggling Engine & State Machine
- **Statuses**: `PENDING`, `COUNTERED`, `ACCEPTED`, `REJECTED`, `EXPIRED`, `WITHDRAWN`, `CONVERTED`.
- **Allowed Transitions**: Enforced via enum transition map.
- **Stock Reservation on Acceptance**: Stock is reserved only when an offer is accepted (never on submission), preventing inventory hostage attacks.
- **Atomic Guarded Update**:
  ```sql
  UPDATE skus SET reserved = reserved + :qty WHERE id = :id AND stock_on_hand - reserved >= :qty;
  ```
- **Auto-Rules**:
  - `>= auto_accept_pct`: Immediate auto-acceptance and stock reservation.
  - `< auto_reject_pct`: Immediate auto-rejection.
  - Floor price never leaves the backend.
- **Anti-Abuse**: Bucket4j rate limiting per buyer, 1 active offer per SKU per buyer, maximum rounds cap.
- **Sweeper**: Background `@Scheduled` sweeper claims expired locks and releases reserved stock.

### Trilingual Search
- **Arabic**: Strips diacritics/tashkeel, unifies alef variants (`أ`, `إ`, `آ` → `ا`), unifies ta marbuta (`ة` → `ه`).
- **French**: Unaccent (`é, è, ê, à, ç` → `e, a, c`).
- **Arabizi**: Transliteration of digit-letters (`3` → `ع`, `7` → `ح`, `9` → `ق`, `5` → `خ`).
- **Aliases**: Resolves colloquial terms (e.g. `zitoun` → Sfax olive oil, `7rissa` → Nabeul harissa).

### Checkout Saga & Invoicing
- Validates cart and applies locked prices from accepted offers.
- Performs COD fraud risk scoring.
- Splits parent order into vendor sub-orders.
- Snapshots price on order lines.
- Issues gapless per-vendor e-invoices with fiscal stamp (1.000 TND) and category VAT.
- Records auditable double-entry ledger entries.

---

## 3. Running & Testing

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker (optional for PostgreSQL; runs H2 in PostgreSQL mode by default)

### Run All Tests
```bash
mvn clean test
```
**Verification covers:**
- `ModularityTest`: Spring Modulith module architecture verification (`ApplicationModules.verify()`).
- `LastJarConcurrencyTest`: 50 concurrent threads competing for the last available SKU unit (exactly 1 wins, 49 fail cleanly).
- `OfferStateMachineTest`: Full parameterized matrix of all state transitions.
- `SweeperTest`: Lock expiration and stock release.
- `CheckoutSagaIntegrationTest`: Complete checkout, sub-order splitting, e-invoicing, and payout ledger.
- `SearchTest`: Arabic, French, and Arabizi normalization.
- `MoneyTest`: Millimes arithmetic, VAT derivation, and serialization.

### Run the Application
```bash
mvn spring-boot:run
```

---

## 4. Key Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/products?q=zitoun&lang=ar` | Trilingual product search |
| `POST` | `/api/v1/offers` | Submit new haggling offer |
| `POST` | `/api/v1/offers/{id}/accept` | Vendor accepts offer (locks price & reserves stock) |
| `POST` | `/api/v1/offers/{id}/counter` | Vendor counters offer |
| `POST` | `/api/v1/offers/{id}/reject` | Vendor rejects offer |
| `POST` | `/api/v1/offers/{id}/withdraw` | Buyer withdraws offer |
| `POST` | `/api/v1/cart/items` | Add item to cart (supports offer locked price) |
| `GET` | `/api/v1/cart` | Get current cart with resolved prices |
| `POST` | `/api/v1/checkout` | Execute checkout saga with `Idempotency-Key` |
| `POST` | `/api/v1/vendor-orders/{id}/ship` | Ship vendor order and book courier |
| `GET` | `/api/v1/invoices/by-order/{id}` | Retrieve generated invoice |
| `GET` | `/api/v1/invoices/{id}/export` | Export invoice document |
