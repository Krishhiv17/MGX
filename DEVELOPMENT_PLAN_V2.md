# MGX Development Plan v2 (Revised Architecture & Requirements)

## 0. Scope & Objectives
This plan updates the original MGX roadmap to match the new product requirements:
- Separate **Admin portal** (higher-security access).
- **Developers + Gamers** share a single public portal.
- A **private API** (API key authenticated) serves banks with game + rate data.
- Games are **country-restricted**; users pick a location at signup and can change it later.
- **Wallets are isolated per country** (no cross-country exchange).
- **Bank app mock** consumes the private API, validates MGX accounts, and uses OTP for purchases.
- All components are **local mocks** (no external infra).

This is a professional-grade spec emphasizing auditable transactions and deterministic settlement.

---

## 1. Current System State (Baseline)
**Backend (Spring Boot)**  
- Users, Developers, Games, Wallets, Ledger, Topups, Purchases, Receivables, Settlements, FX, Rates.
- JWT auth with roles (USER/DEVELOPER/ADMIN).
- Idempotency for topups/purchases.
- FX windows cached + refreshed via mock FX service.
- Bank mock for payouts + points.
- Admin endpoints for rates/games/developers.

**Frontend (Next.js)**  
- User auth, dashboard, topup, purchase, transactions.
- Rate preview and receipts.
- Admin/Developer UIs not yet implemented.

**Migrations**  
- Flyway migrations up to V4 (approval workflow).

---

## 2. Architecture Changes Required

### 2.1 Security & Access
- **Admin Portal**: separate frontend app (or route group with stronger access constraints).
- **Private API**: API key authentication (header-based), restricted to bank and admin service calls.
- **OTP verification**: mock OTP service for bank purchase flow and account verification.

### 2.2 Data Model Changes
New fields/tables required:
- **Users**
  - `phone_number` (unique, required)
  - `country_code` (ISO-3166) for current location
  - `phone_verified_at` (OTP verified)
- **Wallets**
  - `country_code` (partition wallets by country)
- **Games**
  - Allowed countries list (developer chooses from MGX-supported pool):
    - Table `game_allowed_countries` (game_id, country_code)
    - Table `mgx_supported_countries` (country_code, name, status)
- **Rates**
  - Per-game rate remains, but must check allowed country.
- **Bank Link**
  - `bank_links` table: user_id, bank_ref, phone_number, verified_at
- **OTP**
  - `otp_sessions` table: session_id, phone, purpose, expires_at, attempts, status
- **API Keys**
  - `api_keys` table: key_hash, owner_name, status, scopes, created_at, revoked_at
 - **Approvals & Rejections**
  - `approval_requests` table: id, type, entity_id, status, requested_by, requested_at, reviewed_by, reviewed_at, rejection_reason
  - Each approval type: developer creation, game creation, rate change, settlement request

### 2.3 Business Logic Changes
- **Country isolation**: wallets are keyed by `user_id + type + game_id + country_code`.
- **Location changes**: user can switch active country; wallets scoped to that country.
- **Game availability**: game list filtered by user country and game allowed countries.
- **Allowed country pool**: developers can only select from `mgx_supported_countries`.
- **Private API**: bank gets games + rates for a country; no user PII.
- **Purchases via bank**:
  1. Bank validates MGX account by phone.
  2. OTP issued/verified.
  3. Bank submits purchase with idempotency key.
  4. Backend books purchase + receivable.
- **Approval workflow**:
  - Admin must approve: developer creation, game creation, rate changes, settlement requests.
  - Admin rejection requires a reason, visible to developer.

---

## 3. Revised Roadmap (Phases)

### Phase A: Foundations & Data Model
**Goal**: Add country isolation, phone identity, and API key support.
- Add columns: users.phone_number, users.country_code, users.phone_verified_at
- Add wallets.country_code
- Add `game_allowed_countries`
- Add `mgx_supported_countries`
- Add `api_keys`, `bank_links`, `otp_sessions` tables
- Add `approval_requests` table
- Backfill existing wallets to default country (e.g., "US")
- Update indexes + constraints for new unique keys

**Deliverables**
- Flyway migration V5__country_and_security.sql
- Updated JPA entities + repositories

---

### Phase B: Auth, API Keys & OTP (Mock)
**Goal**: Secure private API + OTP flows.
- API key filter (header: `X-API-Key`)
- OTP service endpoints:
  - `/v1/otp/request`
  - `/v1/otp/verify`
- Bank link verification endpoints
- Phone number required at registration

**Deliverables**
- ApiKeyAuthFilter + middleware
- OTP mock service + tests
- Updated auth DTOs and registration flow

---

### Phase C: Country-Scoped Wallets & Location Management
**Goal**: Enforce isolation and support location switching.
- WalletService updated to use country_code
- User profile endpoints:
  - GET/PUT `/v1/profile`
  - update country_code
- Purchases/topups use current country_code

**Deliverables**
- Wallet model update + composite unique index
- ProfileController
- Updated topup/purchase flows

---

### Phase D: Game Restrictions by Country
**Goal**: Filter games and purchases by allowed countries.
- Game creation includes allowed countries (developer submission from MGX-supported list)
- Admin approval includes allowed countries
- Public game list filtered by user country
- Purchase blocked if game not allowed in user country

**Deliverables**
- game_allowed_countries table
- GameService validation changes

---

### Phase E: Private Bank API (API-key secured)
**Goal**: Provide bank-facing read APIs and purchase APIs.
- `/v1/private/games?country=XX`
- `/v1/private/rates?gameId=...`
- `/v1/private/points-mgc-rate`
- `/v1/private/purchase` (OTP required)

**Deliverables**
- BankPrivateController
- ApiKey scopes validation

---

### Phase F: Bank App Mock
**Goal**: Mock bank app calls to private API.
- Simple UI or CLI mock
- OTP request/verify
- Purchase submission

**Deliverables**
- services/bank-app-mock (Next.js or Spring Boot mock)
- Demo scripts in `MANUAL_TESTS.md`

---

### Phase G: Admin Portal (Secure)
**Goal**: Admin approval workflows + monitoring.
- Approve/reject developers, games, rates, settlement requests
- Rejection reason required and visible to developers
- View developer totals + global transactions
- Manage API keys

**Deliverables**
- frontend-admin app
- Admin API updates

---

### Phase H: Developer + Gamer Portal (Shared)
**Goal**: Unified portal with role‑based UI.
- Gamers: wallets, topup, purchase, location, history
- Developers: create games, propose rates, request settlement, view approval status/rejection reasons

**Deliverables**
- frontend app updates

---

### Phase I: Audit & Compliance
**Goal**: End-to-end auditability.
- Enhanced ledger entries and receipt views
- Admin transaction explorer
- Exportable audit logs

---

### Phase J: Testing & Demo Readiness
**Goal**: End-to-end tests and demo flows.
- Integration tests for OTP, API keys, cross-country wallet isolation
- Manual test scripts for admin, developer, gamer, bank flows

---

## 4. Immediate Next Steps (Recommended)
1. Apply schema migration for country + phone + API key + OTP + approvals tables.
2. Seed `mgx_supported_countries`.
3. Update registration flow to require phone + country.
4. Add wallet country_code logic.
5. Add approval request pipeline (create + review endpoints).
6. Add API key filter + private API endpoints.

---

## 5. Notes for Supervisor Review
This roadmap introduces enterprise‑grade controls:
- Country‑segmented wallets (regulatory isolation).
- API key‑gated private API for banks.
- Admin approval workflows for games/rates/developers.
- OTP verification for bank‑initiated purchases.
- Full audit trail across topups, purchases, receivables.
