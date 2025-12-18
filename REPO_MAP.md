# MGX Repository Map & Analysis

## 1. System Summary

### Glossary

**Actors:**
- **User**: Holds Reward Points, MGC wallet, and per-game UGC wallets. Can top-up (Points→MGC) and purchase (MGC→UGC).
- **Developer**: Owns games, receives receivables from purchases, triggers on-demand batch settlements.
- **Admin**: Configures games, developers, and manual exchange rates.

**Assets:**
- **Reward Points**: User's bank loyalty points (internal balance, no external integration).
- **MGC (Mastercard Game Coin)**: Universal game currency stored in user's MGC wallet.
- **UGC (Unique Game Coin)**: Game-specific currency, stored per-game in user's UGC wallet.

**Financial Concepts:**
- **Receivable**: Amount owed to developer from a purchase. Lifecycle: UNSETTLED → RESERVED → SETTLED.
- **FX Window**: Time-bounded snapshot of USD-base exchange rates, refreshed twice daily. Each receivable stores the FX window ID and rate used for deterministic settlement.
- **Settlement Batch**: Developer-triggered collection of receivables that are reserved, processed, and paid out atomically.

**Infrastructure:**
- **Ledger**: Immutable append-only audit trail of all wallet movements (DEBIT/CREDIT entries).
- **Wallet**: Fast-read balance cache with optimistic locking (version field) for concurrency safety.

### Core Flows

**1. Top-up Flow (Reward Points → MGC)**
```
User → POST /v1/wallets/mgc/topup (with Idempotency-Key)
  → Check idempotency (Redis/DB)
  → Load active points→MGC rate (Redis cache → DB fallback)
  → Validate sufficient reward points
  → Single DB transaction:
     - Create topup record (status=COMPLETED)
     - DEBIT ledger entry (Reward Points wallet)
     - CREDIT ledger entry (MGC wallet)
     - Update wallet balances (optimistic lock)
  → Store idempotency mapping
  → Return topup result
```

**2. Purchase Flow (MGC → UGC + Receivable Creation)**
```
User → POST /v1/games/{gameId}/purchase (with Idempotency-Key)
  → Check idempotency
  → Load active MGC→UGC rate for game
  → Validate sufficient MGC
  → Compute UGC to credit
  → Load current FX window (Redis cache → DB)
  → Compute receivable amount_due in developer settlement currency
  → Single DB transaction:
     - Create purchase record (status=COMPLETED)
     - DEBIT ledger entry (MGC wallet)
     - CREDIT ledger entry (UGC wallet for game)
     - Create receivable (status=UNSETTLED, fx_window_id, fx_rate_used snapshot)
  → Store idempotency mapping
  → Return purchase result
```

**3. Settlement Flow (Developer-triggered Batch)**
```
Developer → POST /v1/developer/settlements/request
  → Create settlement_batch (status=REQUESTED)
  → Atomically reserve receivables:
     SELECT ... FOR UPDATE SKIP LOCKED
     WHERE developer_id = X AND status = 'UNSETTLED'
     UPDATE status = 'RESERVED', settlement_batch_id = Y
  → Publish settlement job to queue (RabbitMQ/Redis Streams)
  → Return batch_id immediately

Worker (async):
  → Mark batch PROCESSING
  → Call Bank Adapter Simulator (HTTP to localhost:8091)
  → On success: Mark receivables SETTLED, batch PAID
  → On failure: Mark batch FAILED, log reason
```

**4. FX Refresh Flow (Twice Daily)**
```
Scheduler (@Scheduled) → Call FX Local Service (localhost:8090)
  → Fetch USD-base quotes
  → Create new fx_rate_window (valid_from=now, valid_to=now+12h)
  → Insert fx_rates rows (USD→X for each currency)
  → Update Redis:
     - fx:window:current = <window_id>
     - fx:rates:<window_id> = {currency → rate}
  → Mark window status (SUCCESS/FAILED)
```

## 2. Expected Backend Modules/Packages

Based on the system design, the backend should be organized as a modular monolith with the following package structure:

```
com.mgx/
├── config/                    ✅ EXISTS (CorsConfig)
│   ├── SecurityConfig         ❌ MISSING (JWT auth, roles)
│   ├── RedisConfig            ❌ MISSING (if using Redis)
│   └── RabbitMQConfig         ❌ MISSING (optional, for async settlement)
│
├── auth/                      ❌ MISSING
│   ├── controller/
│   │   └── AuthController     (POST /login, POST /register)
│   ├── service/
│   │   └── AuthService        (JWT generation, password hashing)
│   ├── security/
│   │   ├── JwtTokenProvider
│   │   └── JwtAuthenticationFilter
│   └── dto/
│       ├── LoginRequest
│       └── AuthResponse
│
├── wallet/                    ❌ MISSING
│   ├── controller/
│   │   └── WalletController   (GET /v1/wallets)
│   ├── service/
│   │   └── WalletService      (balance queries, wallet initialization)
│   ├── repository/
│   │   └── WalletRepository
│   └── model/
│       └── Wallet
│
├── ledger/                    ❌ MISSING
│   ├── service/
│   │   └── LedgerService      (append-only entries, audit queries)
│   ├── repository/
│   │   └── LedgerEntryRepository
│   └── model/
│       └── LedgerEntry
│
├── rates/                     ❌ MISSING
│   ├── controller/
│   │   └── RateController     (Admin: POST /v1/admin/rates/*)
│   ├── service/
│   │   └── RateService        (active rate lookup, cache management)
│   ├── repository/
│   │   ├── RatePointsMgcRepository
│   │   └── RateMgcUgcRepository
│   └── model/
│       ├── RatePointsMgc
│       └── RateMgcUgc
│
├── fx/                        ❌ MISSING
│   ├── controller/
│   │   └── FxController       (Admin: POST /v1/admin/fx/refresh)
│   ├── service/
│   │   ├── FxService          (window management, rate conversion)
│   │   └── FxRefreshScheduler (@Scheduled 2x/day)
│   ├── client/
│   │   └── FxLocalClient      (HTTP client to localhost:8090)
│   ├── repository/
│   │   ├── FxRateWindowRepository
│   │   └── FxRateRepository
│   └── model/
│       ├── FxRateWindow
│       └── FxRate
│
├── topup/                     ❌ MISSING
│   ├── controller/
│   │   └── TopupController    (POST /v1/wallets/mgc/topup)
│   ├── service/
│   │   └── TopupService       (idempotency, atomic transaction)
│   ├── repository/
│   │   └── TopupRepository
│   └── model/
│       └── Topup
│
├── purchase/                  ❌ MISSING
│   ├── controller/
│   │   └── PurchaseController (POST /v1/games/{gameId}/purchase)
│   ├── service/
│   │   └── PurchaseService    (idempotency, atomic transaction, receivable creation)
│   ├── repository/
│   │   └── PurchaseRepository
│   └── model/
│       └── Purchase
│
├── settlement/                ❌ MISSING
│   ├── controller/
│   │   └── SettlementController (Developer endpoints)
│   ├── service/
│   │   ├── SettlementService  (reservation, batch creation)
│   │   └── SettlementWorker   (async processing, Bank Adapter call)
│   ├── client/
│   │   └── BankAdapterClient  (HTTP client to localhost:8091)
│   ├── repository/
│   │   ├── ReceivableRepository
│   │   └── SettlementBatchRepository
│   └── model/
│       ├── Receivable
│       └── SettlementBatch
│
├── game/                      ❌ MISSING
│   ├── controller/
│   │   └── GameController     (Admin: POST /v1/admin/games)
│   ├── repository/
│   │   └── GameRepository
│   └── model/
│       └── Game
│
├── developer/                 ❌ MISSING
│   ├── repository/
│   │   └── DeveloperRepository
│   └── model/
│       └── Developer
│
├── user/                      ❌ MISSING
│   ├── repository/
│   │   └── UserRepository
│   └── model/
│       └── User
│
└── common/                    ❌ MISSING
    ├── exception/
    │   ├── GlobalExceptionHandler
    │   ├── InsufficientBalanceException
    │   ├── IdempotencyConflictException
    │   └── RateNotFoundException
    ├── dto/
    │   └── ApiResponse
    └── util/
        └── IdempotencyUtil
```

## 3. Mismatches & Gaps Analysis

### ✅ What EXISTS

1. **Database Schema** (`V1__init.sql`)
   - ✅ Complete schema matching design: users, developers, games, wallets, ledger_entries, rates, topups, purchases, fx_rate_windows, fx_rates, receivables, settlement_batches
   - ✅ Proper constraints, indexes, foreign keys
   - ✅ Optimistic locking (version field on wallets)
   - ✅ Unique constraints for idempotency (topups, purchases)

2. **Seed Data** (`V2__seed.sql`)
   - ✅ Sample users (admin, developer, user)
   - ✅ Sample developer, game, wallets, rates, FX window

3. **Infrastructure Setup**
   - ✅ Spring Boot 3.x with JPA, Security, Flyway
   - ✅ PostgreSQL configuration
   - ✅ CORS config (allows Authorization, Idempotency-Key headers)
   - ✅ Actuator endpoints configured

4. **Frontend Skeleton**
   - ✅ Next.js 16 with TypeScript
   - ✅ Tailwind CSS configured

### ❌ What's MISSING

#### Backend (Critical Gaps)

1. **Authentication & Authorization**
   - ❌ No JWT implementation
   - ❌ No SecurityConfig
   - ❌ No role-based access control
   - ❌ No User entity/repository
   - ❌ No password hashing/validation

2. **Core Business Logic**
   - ❌ No entity classes (Wallet, LedgerEntry, Topup, Purchase, Receivable, etc.)
   - ❌ No repositories (JPA repositories)
   - ❌ No service layer
   - ❌ No controllers/endpoints
   - ❌ No DTOs

3. **Wallet & Ledger Module**
   - ❌ No wallet balance queries
   - ❌ No ledger entry creation
   - ❌ No atomic transaction orchestration

4. **Rates Module**
   - ❌ No rate lookup logic
   - ❌ No Redis caching for active rates
   - ❌ No admin endpoints for rate management

5. **FX Module**
   - ❌ No FX window management
   - ❌ No scheduled refresh job (@Scheduled)
   - ❌ No FX Local Service client
   - ❌ No Redis caching for FX windows
   - ❌ No rate conversion logic (USD-base computation)

6. **Topup Module**
   - ❌ No topup endpoint
   - ❌ No idempotency handling
   - ❌ No atomic transaction (ledger + wallet + topup)

7. **Purchase Module**
   - ❌ No purchase endpoint
   - ❌ No receivable creation
   - ❌ No FX snapshot storage

8. **Settlement Module**
   - ❌ No settlement endpoints
   - ❌ No receivable reservation logic (SELECT FOR UPDATE SKIP LOCKED)
   - ❌ No async worker
   - ❌ No Bank Adapter client
   - ❌ No queue integration (RabbitMQ/Redis Streams)

9. **Game & Developer Management**
   - ❌ No admin endpoints for game/developer CRUD
   - ❌ No entity classes

10. **Infrastructure Dependencies**
    - ❌ Redis dependency not in pom.xml (if using Redis)
    - ❌ RabbitMQ dependency not in pom.xml (if using RabbitMQ)
    - ❌ JWT library (e.g., jjwt) not in pom.xml
    - ❌ Resilience4j not in pom.xml (for retries/circuit breakers)

11. **Error Handling**
    - ❌ No global exception handler
    - ❌ No custom exceptions
    - ❌ No standardized API response format

12. **Idempotency**
    - ❌ No Redis-based idempotency store
    - ❌ No idempotency utility/service

#### Frontend (Critical Gaps)

1. **No UI Implementation**
   - ❌ Default Next.js template only
   - ❌ No pages for: wallet view, top-up, purchase, transaction history
   - ❌ No developer dashboard (receivables, settlement)
   - ❌ No admin panel (rate management, game management)
   - ❌ No authentication UI (login, register)

2. **No API Integration**
   - ❌ No API client/service layer
   - ❌ No JWT token storage/management
   - ❌ No HTTP interceptors for auth headers

3. **No State Management**
   - ❌ No user context/auth state
   - ❌ No wallet balance state

#### External Services (Missing)

1. **FX Local Service** (`services/fx-mock/`)
   - ❌ Directory exists but no implementation
   - Should run on port 8090, provide USD-base quotes

2. **Bank Adapter Simulator** (`services/bank-mock/`)
   - ❌ Directory exists but no implementation
   - Should run on port 8091, simulate payouts

## 4. Next Steps (Priority Order)

### Phase 1: Foundation (Authentication & Entities)
1. Add JWT dependencies to `pom.xml` (jjwt)
2. Create User entity + repository
3. Implement SecurityConfig with JWT filter
4. Create AuthController + AuthService (login endpoint)
5. Create all entity classes (Wallet, LedgerEntry, Topup, Purchase, Receivable, SettlementBatch, Game, Developer, RatePointsMgc, RateMgcUgc, FxRateWindow, FxRate)
6. Create all JPA repositories

### Phase 2: Core Wallet Operations
7. Implement WalletService (balance queries)
8. Implement LedgerService (append entries)
9. Create WalletController (GET /v1/wallets)
10. Implement TopupService with atomic transaction
11. Create TopupController (POST /v1/wallets/mgc/topup) with idempotency
12. Add Redis dependency + IdempotencyService

### Phase 3: Purchase & Receivables
13. Implement PurchaseService with atomic transaction + receivable creation
14. Create PurchaseController (POST /v1/games/{gameId}/purchase) with idempotency
15. Implement FX service (window lookup, rate conversion)
16. Add FX refresh scheduler (@Scheduled)

### Phase 4: Rates & FX
17. Implement RateService (active rate lookup, Redis caching)
18. Create RateController (Admin: POST /v1/admin/rates/*)
19. Implement FxController (Admin: POST /v1/admin/fx/refresh)
20. Create FX Local Service mock (port 8090)

### Phase 5: Settlement
21. Implement ReceivableRepository with reservation query (FOR UPDATE SKIP LOCKED)
22. Implement SettlementService (batch creation, reservation)
23. Create SettlementController (Developer endpoints)
24. Implement SettlementWorker (async processing)
25. Add RabbitMQ or Redis Streams for async settlement
26. Create Bank Adapter mock (port 8091)

### Phase 6: Admin & Frontend
27. Create GameController (Admin: POST /v1/admin/games)
28. Build frontend: Auth pages (login/register)
29. Build frontend: User wallet dashboard
30. Build frontend: Top-up page
31. Build frontend: Purchase page
32. Build frontend: Developer dashboard (receivables, settlement)
33. Build frontend: Admin panel (rates, games)

### Phase 7: Polish
34. Add global exception handler
35. Add structured logging
36. Add API documentation (OpenAPI/Swagger)
37. Add integration tests
38. Add resilience patterns (Retry, Circuit Breaker) for external service calls

---

**Current State**: ~5% complete (schema + seed only)  
**Target State**: Full implementation per system design  
**Estimated Effort**: Significant (all business logic, all endpoints, full frontend)

