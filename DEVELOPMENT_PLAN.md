# MGX Development Plan & Timeline

## Overview

**Current State**: Repository setup complete (schema, seed data, basic skeleton)  
**Target State**: Fully functional MGX platform with all features  
**Estimated Total Time**: 6-8 weeks (assuming full-time development)

---

## Phase 0: Infrastructure & Dependencies Setup
**Duration**: 1-2 days  
**Goal**: Add all required dependencies and configure infrastructure

### Tasks

#### 0.1 Add Maven Dependencies
- [ ] Add JWT library (io.jsonwebtoken:jjwt-api, jjwt-impl, jjwt-jackson)
- [ ] Add Redis client (spring-boot-starter-data-redis)
- [ ] Add RabbitMQ (spring-boot-starter-amqp) - optional, can use Redis Streams
- [ ] Add Resilience4j (resilience4j-spring-boot3, resilience4j-retry, resilience4j-circuitbreaker)
- [ ] Add OpenAPI/Swagger (springdoc-openapi-starter-webmvc-ui)
- [ ] Add Lombok (optional but recommended for cleaner code)

**Files to modify:**
- `backend/pom.xml`

**Time estimate**: 30 minutes

#### 0.2 Configure Redis in application.yml
- [ ] Add Redis connection properties
- [ ] Configure Redis template bean
- [ ] Add Redis configuration class

**Files to create:**
- `backend/src/main/java/com/mgx/config/RedisConfig.java`
- Update `backend/src/main/resources/application.yml`

**Time estimate**: 1 hour

#### 0.3 Configure RabbitMQ (Optional)
- [ ] Add RabbitMQ connection properties
- [ ] Create queue configuration (settlement.batch.requested, settlement.batch.dlq)
- [ ] Create RabbitMQ config class

**Files to create:**
- `backend/src/main/java/com/mgx/config/RabbitMQConfig.java`
- Update `backend/src/main/resources/application.yml`

**Time estimate**: 1 hour (if using RabbitMQ)

#### 0.4 Setup External Service Mocks
- [ ] Create FX Local Service (simple Spring Boot app on port 8090)
  - Endpoint: GET /fx/rates (returns USD-base quotes)
  - Can read from file or generate mock data
- [ ] Create Bank Adapter Simulator (simple Spring Boot app on port 8091)
  - Endpoint: POST /payouts (simulates bank transfer)
  - Returns success/failure response

**Files to create:**
- `services/fx-mock/pom.xml`
- `services/fx-mock/src/main/java/.../FxMockApplication.java`
- `services/fx-mock/src/main/java/.../FxController.java`
- `services/bank-mock/pom.xml`
- `services/bank-mock/src/main/java/.../BankMockApplication.java`
- `services/bank-mock/src/main/java/.../PayoutController.java`

**Time estimate**: 4-6 hours

**Deliverable**: All dependencies added, Redis configured, external mocks running

---

## Phase 1: Foundation - Entities & Repositories
**Duration**: 2-3 days  
**Goal**: Create all JPA entities and repositories matching the schema

### Tasks

#### 1.1 Create Base Entity Classes
- [ ] Create `User` entity (id, email, passwordHash, role, createdAt)
- [ ] Create `Developer` entity (id, name, settlementCurrency, bankAccountRef, status, createdAt)
- [ ] Create `Game` entity (id, developerId, name, status, settlementCurrency, createdAt)
- [ ] Create `Wallet` entity (id, userId, type, gameId, balance, version, createdAt)
  - Add unique constraint handling for (userId, type, gameId)
- [ ] Create `LedgerEntry` entity (id, refType, refId, walletId, direction, assetType, amount, createdAt)

**Files to create:**
- `backend/src/main/java/com/mgx/user/model/User.java`
- `backend/src/main/java/com/mgx/developer/model/Developer.java`
- `backend/src/main/java/com/mgx/game/model/Game.java`
- `backend/src/main/java/com/mgx/wallet/model/Wallet.java`
- `backend/src/main/java/com/mgx/ledger/model/LedgerEntry.java`

**Time estimate**: 4-5 hours

#### 1.2 Create Transaction Entities
- [ ] Create `Topup` entity (id, userId, pointsDebited, mgcCredited, rateSnapshot, rateId, status, idempotencyKey, createdAt)
- [ ] Create `Purchase` entity (id, userId, gameId, mgcSpent, ugcCredited, rateSnapshot, rateId, status, idempotencyKey, createdAt)

**Files to create:**
- `backend/src/main/java/com/mgx/topup/model/Topup.java`
- `backend/src/main/java/com/mgx/purchase/model/Purchase.java`

**Time estimate**: 2 hours

#### 1.3 Create Rate Entities
- [ ] Create `RatePointsMgc` entity (id, pointsPerMgc, activeFrom, activeTo, createdBy, createdAt)
- [ ] Create `RateMgcUgc` entity (id, gameId, ugcPerMgc, activeFrom, activeTo, createdBy, createdAt)

**Files to create:**
- `backend/src/main/java/com/mgx/rates/model/RatePointsMgc.java`
- `backend/src/main/java/com/mgx/rates/model/RateMgcUgc.java`

**Time estimate**: 2 hours

#### 1.4 Create FX Entities
- [ ] Create `FxRateWindow` entity (id, provider, fetchedAt, validFrom, validTo, status)
- [ ] Create `FxRate` entity (windowId, baseCurrency, quoteCurrency, rate) - composite key

**Files to create:**
- `backend/src/main/java/com/mgx/fx/model/FxRateWindow.java`
- `backend/src/main/java/com/mgx/fx/model/FxRate.java`

**Time estimate**: 2 hours

#### 1.5 Create Settlement Entities
- [ ] Create `Receivable` entity (id, purchaseId, developerId, amountDue, settlementCurrency, fxWindowId, fxRateUsed, status, reservedAt, settledAt, settlementBatchId, createdAt)
- [ ] Create `SettlementBatch` entity (id, developerId, requestedBy, status, currency, totalAmount, requestedAt, processedAt, failureReason)

**Files to create:**
- `backend/src/main/java/com/mgx/settlement/model/Receivable.java`
- `backend/src/main/java/com/mgx/settlement/model/SettlementBatch.java`

**Time estimate**: 2 hours

#### 1.6 Create All JPA Repositories
- [ ] UserRepository extends JpaRepository<User, UUID>
- [ ] DeveloperRepository extends JpaRepository<Developer, UUID>
- [ ] GameRepository extends JpaRepository<Game, UUID>
- [ ] WalletRepository extends JpaRepository<Wallet, UUID>
  - Add custom query: findByUserIdAndTypeAndGameId
- [ ] LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID>
  - Add custom query: findByWalletIdOrderByCreatedAtDesc
- [ ] TopupRepository extends JpaRepository<Topup, UUID>
  - Add custom query: findByUserIdAndIdempotencyKey
- [ ] PurchaseRepository extends JpaRepository<Purchase, UUID>
  - Add custom query: findByUserIdAndIdempotencyKey
- [ ] RatePointsMgcRepository extends JpaRepository<RatePointsMgc, UUID>
  - Add custom query: findActiveRate (activeFrom <= now AND (activeTo IS NULL OR activeTo > now))
- [ ] RateMgcUgcRepository extends JpaRepository<RateMgcUgc, UUID>
  - Add custom query: findActiveRateByGameId
- [ ] FxRateWindowRepository extends JpaRepository<FxRateWindow, UUID>
  - Add custom query: findCurrentWindow (validFrom <= now AND validTo > now AND status = 'SUCCESS')
- [ ] FxRateRepository extends JpaRepository<FxRate, FxRateId>
  - Add custom query: findByWindowId
- [ ] ReceivableRepository extends JpaRepository<Receivable, UUID>
  - Add custom query: findUnsettledByDeveloperId (for reservation)
  - Add custom query: findBySettlementBatchId
- [ ] SettlementBatchRepository extends JpaRepository<SettlementBatch, UUID>
  - Add custom query: findByDeveloperIdOrderByRequestedAtDesc

**Files to create:**
- `backend/src/main/java/com/mgx/user/repository/UserRepository.java`
- `backend/src/main/java/com/mgx/developer/repository/DeveloperRepository.java`
- `backend/src/main/java/com/mgx/game/repository/GameRepository.java`
- `backend/src/main/java/com/mgx/wallet/repository/WalletRepository.java`
- `backend/src/main/java/com/mgx/ledger/repository/LedgerEntryRepository.java`
- `backend/src/main/java/com/mgx/topup/repository/TopupRepository.java`
- `backend/src/main/java/com/mgx/purchase/repository/PurchaseRepository.java`
- `backend/src/main/java/com/mgx/rates/repository/RatePointsMgcRepository.java`
- `backend/src/main/java/com/mgx/rates/repository/RateMgcUgcRepository.java`
- `backend/src/main/java/com/mgx/fx/repository/FxRateWindowRepository.java`
- `backend/src/main/java/com/mgx/fx/repository/FxRateRepository.java`
- `backend/src/main/java/com/mgx/settlement/repository/ReceivableRepository.java`
- `backend/src/main/java/com/mgx/settlement/repository/SettlementBatchRepository.java`

**Time estimate**: 4-5 hours

**Deliverable**: All entities and repositories created, application compiles and connects to DB

---

## Phase 2: Authentication & Security
**Duration**: 2-3 days  
**Goal**: Implement JWT-based authentication with role-based access control

### Tasks

#### 2.1 Create Security Configuration
- [ ] Create `SecurityConfig` class
  - Configure JWT filter chain
  - Set up public endpoints (login, register, health)
  - Configure role-based access (USER, DEVELOPER, ADMIN)
  - Disable CSRF (using JWT in header)
  - Configure password encoder (BCrypt)

**Files to create:**
- `backend/src/main/java/com/mgx/config/SecurityConfig.java`

**Time estimate**: 3-4 hours

#### 2.2 Create JWT Utilities
- [ ] Create `JwtTokenProvider` class
  - Generate JWT token (with userId, email, roles)
  - Validate JWT token
  - Extract claims from token
- [ ] Create `JwtAuthenticationFilter` class
  - Extract token from Authorization header
  - Validate and set authentication in SecurityContext

**Files to create:**
- `backend/src/main/java/com/mgx/auth/security/JwtTokenProvider.java`
- `backend/src/main/java/com/mgx/auth/security/JwtAuthenticationFilter.java`

**Time estimate**: 4-5 hours

#### 2.3 Create Auth Service
- [ ] Create `AuthService` class
  - `register(email, password, role)` - hash password, create user, create default wallets
  - `login(email, password)` - validate credentials, generate JWT
  - `validateToken(token)` - validate JWT and return user info

**Files to create:**
- `backend/src/main/java/com/mgx/auth/service/AuthService.java`

**Time estimate**: 3-4 hours

#### 2.4 Create Auth DTOs
- [ ] Create `LoginRequest` (email, password)
- [ ] Create `RegisterRequest` (email, password, role)
- [ ] Create `AuthResponse` (token, userId, email, role)

**Files to create:**
- `backend/src/main/java/com/mgx/auth/dto/LoginRequest.java`
- `backend/src/main/java/com/mgx/auth/dto/RegisterRequest.java`
- `backend/src/main/java/com/mgx/auth/dto/AuthResponse.java`

**Time estimate**: 1 hour

#### 2.5 Create Auth Controller
- [ ] Create `AuthController` class
  - POST `/v1/auth/register` - public endpoint
  - POST `/v1/auth/login` - public endpoint
  - GET `/v1/auth/me` - protected, returns current user info

**Files to create:**
- `backend/src/main/java/com/mgx/auth/controller/AuthController.java`

**Time estimate**: 2 hours

#### 2.6 Create Wallet Initialization Service
- [ ] Create `WalletInitializationService` (called on user registration)
  - Create REWARD_POINTS wallet (initial balance from seed or 0)
  - Create MGC wallet (balance 0)
  - Note: UGC wallets created on first purchase per game

**Files to create:**
- `backend/src/main/java/com/mgx/wallet/service/WalletInitializationService.java`

**Time estimate**: 2 hours

#### 2.7 Test Authentication
- [ ] Test registration endpoint
- [ ] Test login endpoint
- [ ] Test JWT token validation
- [ ] Test protected endpoints with/without token
- [ ] Test role-based access

**Time estimate**: 2-3 hours

**Deliverable**: Users can register, login, and receive JWT tokens. Protected endpoints enforce authentication.

---

## Phase 3: Common Infrastructure
**Duration**: 1-2 days  
**Goal**: Create shared utilities, exception handling, and DTOs

### Tasks

#### 3.1 Create Exception Classes
- [ ] Create `InsufficientBalanceException`
- [ ] Create `IdempotencyConflictException`
- [ ] Create `RateNotFoundException`
- [ ] Create `WalletNotFoundException`
- [ ] Create `GameNotFoundException`
- [ ] Create `FxWindowNotFoundException`
- [ ] Create `ReceivableNotFoundException`

**Files to create:**
- `backend/src/main/java/com/mgx/common/exception/InsufficientBalanceException.java`
- `backend/src/main/java/com/mgx/common/exception/IdempotencyConflictException.java`
- `backend/src/main/java/com/mgx/common/exception/RateNotFoundException.java`
- `backend/src/main/java/com/mgx/common/exception/WalletNotFoundException.java`
- `backend/src/main/java/com/mgx/common/exception/GameNotFoundException.java`
- `backend/src/main/java/com/mgx/common/exception/FxWindowNotFoundException.java`
- `backend/src/main/java/com/mgx/common/exception/ReceivableNotFoundException.java`

**Time estimate**: 2 hours

#### 3.2 Create Global Exception Handler
- [ ] Create `GlobalExceptionHandler` with `@ControllerAdvice`
  - Handle validation errors (400)
  - Handle authentication errors (401)
  - Handle authorization errors (403)
  - Handle not found errors (404)
  - Handle business logic errors (400/409)
  - Handle server errors (500)
  - Return standardized `ApiError` response

**Files to create:**
- `backend/src/main/java/com/mgx/common/exception/GlobalExceptionHandler.java`
- `backend/src/main/java/com/mgx/common/dto/ApiError.java`

**Time estimate**: 3-4 hours

#### 3.3 Create Standardized API Response DTOs
- [ ] Create `ApiResponse<T>` wrapper class
- [ ] Create `ApiSuccessResponse` and `ApiErrorResponse` if needed

**Files to create:**
- `backend/src/main/java/com/mgx/common/dto/ApiResponse.java`

**Time estimate**: 1 hour

#### 3.4 Create Idempotency Service
- [ ] Create `IdempotencyService` class
  - `checkAndStore(userId, operation, key, result)` - check Redis, store if new
  - `getExistingResult(userId, operation, key)` - retrieve from Redis
  - Use Redis with TTL (e.g., 24 hours)
  - Keys: `idempo:topup:{userId}:{key}`, `idempo:purchase:{userId}:{key}`

**Files to create:**
- `backend/src/main/java/com/mgx/common/service/IdempotencyService.java`

**Time estimate**: 3-4 hours

#### 3.5 Create Validation Utilities
- [ ] Create validation for positive amounts
- [ ] Create validation for currency codes
- [ ] Create validation for wallet types

**Files to create:**
- `backend/src/main/java/com/mgx/common/util/ValidationUtil.java`

**Time estimate**: 1-2 hours

**Deliverable**: Exception handling, idempotency service, and common utilities ready for use

---

## Phase 4: Wallet & Ledger Services
**Duration**: 2 days  
**Goal**: Implement wallet balance queries and ledger entry creation

### Tasks

#### 4.1 Create Wallet Service
- [ ] Create `WalletService` class
  - `getWalletsByUserId(userId)` - return all wallets for user
  - `getWalletByUserAndType(userId, type, gameId)` - get specific wallet
  - `getOrCreateWallet(userId, type, gameId)` - get existing or create new
  - `updateBalance(walletId, amount, direction)` - update with optimistic locking
  - Handle version conflicts (retry logic or throw exception)

**Files to create:**
- `backend/src/main/java/com/mgx/wallet/service/WalletService.java`

**Time estimate**: 4-5 hours

#### 4.2 Create Ledger Service
- [ ] Create `LedgerService` class
  - `createEntry(refType, refId, walletId, direction, assetType, amount)` - append-only
  - `getLedgerEntriesByWallet(walletId, limit)` - for audit/history
  - `getLedgerEntriesByReference(refType, refId)` - for transaction details

**Files to create:**
- `backend/src/main/java/com/mgx/ledger/service/LedgerService.java`

**Time estimate**: 3-4 hours

#### 4.3 Create Wallet Controller
- [ ] Create `WalletController` class
  - GET `/v1/wallets` - return all wallets for authenticated user
  - GET `/v1/wallets/{walletId}` - return specific wallet (with authorization check)
  - Add `@PreAuthorize("hasRole('USER')")` or similar

**Files to create:**
- `backend/src/main/java/com/mgx/wallet/controller/WalletController.java`
- `backend/src/main/java/com/mgx/wallet/dto/WalletResponse.java`

**Time estimate**: 2-3 hours

#### 4.4 Test Wallet & Ledger
- [ ] Test wallet creation
- [ ] Test balance updates with optimistic locking
- [ ] Test ledger entry creation
- [ ] Test concurrent updates (if possible)

**Time estimate**: 2-3 hours

**Deliverable**: Users can query wallet balances, ledger entries are created correctly

---

## Phase 5: Rates Module
**Duration**: 2 days  
**Goal**: Implement rate lookup with Redis caching

### Tasks

#### 5.1 Create Rate Service
- [ ] Create `RateService` class
  - `getActivePointsToMgcRate()` - check Redis cache, fallback to DB
  - `getActiveMgcToUgcRate(gameId)` - check Redis cache, fallback to DB
  - Cache keys: `rate:points_mgc:active`, `rate:mgc_ugc:{gameId}:active`
  - Cache TTL: 1 hour (or until rate expires)
  - Invalidate cache when new rate is created

**Files to create:**
- `backend/src/main/java/com/mgx/rates/service/RateService.java`

**Time estimate**: 4-5 hours

#### 5.2 Create Rate Controller (Admin)
- [ ] Create `RateController` class
  - POST `/v1/admin/rates/points-mgc` - create new points→MGC rate
    - Request body: `pointsPerMgc`, `activeFrom` (optional)
    - Deactivate previous rate (set activeTo = now)
    - Invalidate Redis cache
  - POST `/v1/admin/rates/mgc-ugc` - create new MGC→UGC rate for game
    - Request body: `gameId`, `ugcPerMgc`, `activeFrom` (optional)
    - Deactivate previous rate for that game
    - Invalidate Redis cache
  - Add `@PreAuthorize("hasRole('ADMIN')")`

**Files to create:**
- `backend/src/main/java/com/mgx/rates/controller/RateController.java`
- `backend/src/main/java/com/mgx/rates/dto/CreatePointsMgcRateRequest.java`
- `backend/src/main/java/com/mgx/rates/dto/CreateMgcUgcRateRequest.java`

**Time estimate**: 3-4 hours

#### 5.3 Test Rates
- [ ] Test rate creation
- [ ] Test active rate lookup
- [ ] Test Redis caching
- [ ] Test rate deactivation

**Time estimate**: 2 hours

**Deliverable**: Admins can create rates, system caches and retrieves active rates efficiently

---

## Phase 6: FX Module
**Duration**: 2-3 days  
**Goal**: Implement FX window management and scheduled refresh

### Tasks

#### 6.1 Create FX Client
- [ ] Create `FxLocalClient` class
  - HTTP client to call `http://localhost:8090/fx/rates`
  - Use RestTemplate or WebClient
  - Add Resilience4j retry and circuit breaker
  - Handle failures gracefully

**Files to create:**
- `backend/src/main/java/com/mgx/fx/client/FxLocalClient.java`
- `backend/src/main/java/com/mgx/fx/dto/FxRateResponse.java`

**Time estimate**: 3-4 hours

#### 6.2 Create FX Service
- [ ] Create `FxService` class
  - `getCurrentFxWindow()` - check Redis cache (`fx:window:current`), fallback to DB
  - `getFxRatesForWindow(windowId)` - check Redis cache (`fx:rates:{windowId}`), fallback to DB
  - `convertCurrency(fromCurrency, toCurrency, amount, windowId)` - compute using USD-base formula
  - `refreshFxRates()` - call FX client, create new window, cache in Redis
  - Handle refresh failures (keep last window, mark status)

**Files to create:**
- `backend/src/main/java/com/mgx/fx/service/FxService.java`

**Time estimate**: 5-6 hours

#### 6.3 Create FX Refresh Scheduler
- [ ] Create `FxRefreshScheduler` class
  - `@Scheduled(cron = "0 0 0,12 * * *")` - twice daily (00:00 and 12:00)
  - Call `FxService.refreshFxRates()`
  - Log success/failure
  - Handle exceptions (don't crash scheduler)

**Files to create:**
- `backend/src/main/java/com/mgx/fx/scheduler/FxRefreshScheduler.java`
- Enable scheduling in main application: `@EnableScheduling`

**Time estimate**: 2-3 hours

#### 6.4 Create FX Controller (Admin)
- [ ] Create `FxController` class
  - POST `/v1/admin/fx/refresh` - manual trigger for testing
  - GET `/v1/admin/fx/windows` - list FX windows (for admin dashboard)
  - Add `@PreAuthorize("hasRole('ADMIN')")`

**Files to create:**
- `backend/src/main/java/com/mgx/fx/controller/FxController.java`

**Time estimate**: 2 hours

#### 6.5 Test FX Module
- [ ] Test FX client call to mock service
- [ ] Test FX window creation
- [ ] Test Redis caching
- [ ] Test currency conversion (USD-base formula)
- [ ] Test scheduled refresh (or manual trigger)
- [ ] Test failure handling

**Time estimate**: 3-4 hours

**Deliverable**: FX rates refresh twice daily, cached in Redis, currency conversion works correctly

---

## Phase 7: Top-up Module
**Duration**: 2-3 days  
**Goal**: Implement Points → MGC conversion with idempotency and atomic transactions

### Tasks

#### 7.1 Create Top-up DTOs
- [ ] Create `TopupRequest` (pointsAmount or mgcAmount - one or the other)
- [ ] Create `TopupResponse` (topupId, pointsDebited, mgcCredited, rateUsed, status)

**Files to create:**
- `backend/src/main/java/com/mgx/topup/dto/TopupRequest.java`
- `backend/src/main/java/com/mgx/topup/dto/TopupResponse.java`

**Time estimate**: 1 hour

#### 7.2 Create Top-up Service
- [ ] Create `TopupService` class
  - `processTopup(userId, request, idempotencyKey)` - main logic:
    1. Check idempotency (return existing result if found)
    2. Load active points→MGC rate (via RateService)
    3. Compute amounts (if pointsAmount given, compute mgcAmount; vice versa)
    4. Get or create wallets (REWARD_POINTS and MGC)
    5. Validate sufficient reward points balance
    6. **Single DB transaction:**
       - Create Topup record (status=INITIATED)
       - DEBIT ledger entry (REWARD_POINTS wallet)
       - CREDIT ledger entry (MGC wallet)
       - Update wallet balances (with optimistic locking)
       - Update Topup status to COMPLETED
    7. Store idempotency mapping
    8. Return result
  - Handle optimistic locking conflicts (retry or fail gracefully)
  - Handle insufficient balance exception

**Files to create:**
- `backend/src/main/java/com/mgx/topup/service/TopupService.java`

**Time estimate**: 6-8 hours (complex transaction logic)

#### 7.3 Create Top-up Controller
- [ ] Create `TopupController` class
  - POST `/v1/wallets/mgc/topup`
    - Extract `Idempotency-Key` header (required)
    - Extract userId from JWT token
    - Call TopupService
    - Return TopupResponse
  - GET `/v1/transactions/topups` - list user's topups
  - Add `@PreAuthorize("hasRole('USER')")`

**Files to create:**
- `backend/src/main/java/com/mgx/topup/controller/TopupController.java`

**Time estimate**: 2-3 hours

#### 7.4 Test Top-up
- [ ] Test successful top-up
- [ ] Test idempotency (same key returns same result)
- [ ] Test insufficient balance
- [ ] Test concurrent top-ups (if possible)
- [ ] Test transaction rollback on failure
- [ ] Test ledger entries created correctly

**Time estimate**: 3-4 hours

**Deliverable**: Users can top-up Points → MGC with idempotency and atomic transactions

---

## Phase 8: Purchase Module
**Duration**: 3-4 days  
**Goal**: Implement MGC → UGC conversion with receivable creation

### Tasks

#### 8.1 Create Purchase DTOs
- [ ] Create `PurchaseRequest` (mgcAmount or ugcAmount)
- [ ] Create `PurchaseResponse` (purchaseId, mgcSpent, ugcCredited, rateUsed, receivableId, status)

**Files to create:**
- `backend/src/main/java/com/mgx/purchase/dto/PurchaseRequest.java`
- `backend/src/main/java/com/mgx/purchase/dto/PurchaseResponse.java`

**Time estimate**: 1 hour

#### 8.2 Create Purchase Service
- [ ] Create `PurchaseService` class
  - `processPurchase(userId, gameId, request, idempotencyKey)` - main logic:
    1. Check idempotency
    2. Validate game exists and is ACTIVE
    3. Load active MGC→UGC rate for game (via RateService)
    4. Compute amounts
    5. Get or create wallets (MGC and UGC for game)
    6. Validate sufficient MGC balance
    7. Get current FX window (via FxService)
    8. Get game's developer and settlement currency
    9. Compute receivable amount_due:
       - If settlement currency = MGC currency (USD), use direct conversion
       - Else, convert using FX: amount_due = (mgcSpent * fxRate)
    10. **Single DB transaction:**
        - Create Purchase record (status=INITIATED)
        - DEBIT ledger entry (MGC wallet)
        - CREDIT ledger entry (UGC wallet)
        - Create Receivable (status=UNSETTLED, fxWindowId, fxRateUsed snapshot)
        - Update wallet balances
        - Update Purchase status to COMPLETED
    11. Store idempotency mapping
    12. Return result

**Files to create:**
- `backend/src/main/java/com/mgx/purchase/service/PurchaseService.java`

**Time estimate**: 8-10 hours (complex with FX and receivable creation)

#### 8.3 Create Purchase Controller
- [ ] Create `PurchaseController` class
  - POST `/v1/games/{gameId}/purchase`
    - Extract `Idempotency-Key` header (required)
    - Extract userId from JWT
    - Call PurchaseService
    - Return PurchaseResponse
  - GET `/v1/transactions/purchases` - list user's purchases
  - Add `@PreAuthorize("hasRole('USER')")`

**Files to create:**
- `backend/src/main/java/com/mgx/purchase/controller/PurchaseController.java`

**Time estimate**: 2-3 hours

#### 8.4 Test Purchase
- [ ] Test successful purchase
- [ ] Test UGC wallet creation on first purchase
- [ ] Test receivable creation with FX snapshot
- [ ] Test idempotency
- [ ] Test insufficient MGC balance
- [ ] Test game not found
- [ ] Test FX conversion for cross-currency receivables
- [ ] Test transaction atomicity

**Time estimate**: 4-5 hours

**Deliverable**: Users can purchase UGC, receivables created with FX snapshots

---

## Phase 9: Settlement Module
**Duration**: 3-4 days  
**Goal**: Implement developer-triggered batch settlement with async processing

### Tasks

#### 9.1 Create Settlement DTOs
- [ ] Create `SettlementRequest` (optional filters)
- [ ] Create `SettlementResponse` (batchId, status, totalAmount, currency, requestedAt)
- [ ] Create `ReceivableResponse` (id, amountDue, currency, status, createdAt)
- [ ] Create `SettlementBatchResponse` (full batch details with receivables)

**Files to create:**
- `backend/src/main/java/com/mgx/settlement/dto/SettlementRequest.java`
- `backend/src/main/java/com/mgx/settlement/dto/SettlementResponse.java`
- `backend/src/main/java/com/mgx/settlement/dto/ReceivableResponse.java`
- `backend/src/main/java/com/mgx/settlement/dto/SettlementBatchResponse.java`

**Time estimate**: 1-2 hours

#### 9.2 Create Receivable Reservation Logic
- [ ] Add custom query to ReceivableRepository:
  - `findUnsettledByDeveloperIdForUpdate(developerId)` - uses `SELECT ... FOR UPDATE SKIP LOCKED`
  - This ensures safe concurrent settlement requests
- [ ] Create `ReceivableReservationService`:
  - `reserveReceivablesForSettlement(developerId, batchId)` - atomic reservation
  - Lock and update receivables to RESERVED status
  - Calculate total amount
  - Return list of reserved receivables

**Files to create:**
- `backend/src/main/java/com/mgx/settlement/service/ReceivableReservationService.java`
- Update `backend/src/main/java/com/mgx/settlement/repository/ReceivableRepository.java`

**Time estimate**: 4-5 hours

#### 9.3 Create Settlement Service
- [ ] Create `SettlementService` class
  - `requestSettlement(developerId, requestedBy)` - main logic:
    1. Create SettlementBatch (status=REQUESTED)
    2. Reserve receivables atomically (via ReceivableReservationService)
    3. If no receivables, return early with empty batch
    4. Update batch with totalAmount and currency
    5. Publish settlement job to queue (RabbitMQ or Redis Streams)
    6. Return batchId immediately
  - `processSettlementBatch(batchId)` - called by worker:
    1. Load batch and receivables
    2. Mark batch PROCESSING
    3. Call Bank Adapter (via BankAdapterClient)
    4. On success: Mark receivables SETTLED, batch PAID
    5. On failure: Mark batch FAILED, log reason
    6. Handle retries (via queue DLQ)

**Files to create:**
- `backend/src/main/java/com/mgx/settlement/service/SettlementService.java`

**Time estimate**: 6-8 hours

#### 9.4 Create Bank Adapter Client
- [ ] Create `BankAdapterClient` class
  - HTTP client to call `http://localhost:8091/payouts`
  - Request: developer bank account, amount, currency
  - Use Resilience4j retry and circuit breaker
  - Handle failures

**Files to create:**
- `backend/src/main/java/com/mgx/settlement/client/BankAdapterClient.java`
- `backend/src/main/java/com/mgx/settlement/dto/PayoutRequest.java`

**Time estimate**: 3-4 hours

#### 9.5 Create Settlement Worker
- [ ] Create `SettlementWorker` class
  - Consume messages from `settlement.batch.requested` queue
  - Call `SettlementService.processSettlementBatch(batchId)`
  - Handle exceptions (send to DLQ after retries)
  - Log processing status

**Files to create:**
- `backend/src/main/java/com/mgx/settlement/worker/SettlementWorker.java`

**Time estimate**: 3-4 hours

#### 9.6 Create Settlement Controller
- [ ] Create `SettlementController` class
  - POST `/v1/developer/settlements/request` - trigger settlement
    - Extract developerId from JWT (user must be DEVELOPER)
    - Call SettlementService.requestSettlement
    - Return SettlementResponse
  - GET `/v1/developer/receivables?status=UNSETTLED` - list unsettled receivables
  - GET `/v1/developer/settlements/{batchId}` - get batch details
  - GET `/v1/developer/settlements` - list all batches for developer
  - Add `@PreAuthorize("hasRole('DEVELOPER')")`

**Files to create:**
- `backend/src/main/java/com/mgx/settlement/controller/SettlementController.java`

**Time estimate**: 3-4 hours

#### 9.7 Test Settlement
- [ ] Test settlement request (creates batch, reserves receivables)
- [ ] Test concurrent settlement requests (should not double-reserve)
- [ ] Test settlement processing (worker calls bank adapter)
- [ ] Test successful payout (receivables marked SETTLED)
- [ ] Test failed payout (batch marked FAILED)
- [ ] Test empty settlement (no receivables)
- [ ] Test queue retry mechanism

**Time estimate**: 4-5 hours

**Deliverable**: Developers can trigger settlements, receivables reserved safely, async processing works

---

## Phase 10: Admin & Game Management
**Duration**: 1-2 days  
**Goal**: Implement admin endpoints for game and developer management

### Tasks

#### 10.1 Create Game Controller (Admin)
- [ ] Create `GameController` class
  - POST `/v1/admin/games` - create new game
    - Request: developerId, name, settlementCurrency
  - GET `/v1/admin/games` - list all games
  - GET `/v1/admin/games/{gameId}` - get game details
  - PUT `/v1/admin/games/{gameId}/status` - activate/deactivate game
  - Add `@PreAuthorize("hasRole('ADMIN')")`

**Files to create:**
- `backend/src/main/java/com/mgx/game/controller/GameController.java`
- `backend/src/main/java/com/mgx/game/dto/CreateGameRequest.java`
- `backend/src/main/java/com/mgx/game/dto/GameResponse.java`

**Time estimate**: 3-4 hours

#### 10.2 Create Developer Controller (Admin)
- [ ] Create `DeveloperController` class
  - POST `/v1/admin/developers` - create new developer
    - Request: name, settlementCurrency, bankAccountRef
  - GET `/v1/admin/developers` - list all developers
  - GET `/v1/admin/developers/{developerId}` - get developer details
  - Add `@PreAuthorize("hasRole('ADMIN')")`

**Files to create:**
- `backend/src/main/java/com/mgx/developer/controller/DeveloperController.java`
- `backend/src/main/java/com/mgx/developer/dto/CreateDeveloperRequest.java`
- `backend/src/main/java/com/mgx/developer/dto/DeveloperResponse.java`

**Time estimate**: 2-3 hours

#### 10.3 Test Admin Endpoints
- [ ] Test game creation
- [ ] Test developer creation
- [ ] Test authorization (only ADMIN can access)

**Time estimate**: 1-2 hours

**Deliverable**: Admins can manage games and developers

---

## Phase 11: Frontend - Authentication
**Duration**: 2-3 days  
**Goal**: Build login, register, and auth state management

### Tasks

#### 11.1 Setup API Client
- [ ] Create API client utility (fetch wrapper)
  - Base URL: `http://localhost:8081`
  - Add Authorization header from token
  - Handle 401 (redirect to login)
  - Handle errors consistently

**Files to create:**
- `frontend/lib/api/client.ts`
- `frontend/lib/api/types.ts`

**Time estimate**: 2-3 hours

#### 11.2 Create Auth API Functions
- [ ] Create auth API functions:
  - `login(email, password)`
  - `register(email, password, role)`
  - `getCurrentUser()`
  - `logout()`

**Files to create:**
- `frontend/lib/api/auth.ts`

**Time estimate**: 2 hours

#### 11.3 Create Auth Context
- [ ] Create AuthContext with React Context API
  - Store user state, token
  - Provide login, logout, register functions
  - Persist token in localStorage
  - Load user on app start

**Files to create:**
- `frontend/contexts/AuthContext.tsx`
- `frontend/providers/AuthProvider.tsx`

**Time estimate**: 3-4 hours

#### 11.4 Create Login Page
- [ ] Create login page (`/login`)
  - Email and password form
  - Error handling
  - Redirect to dashboard on success

**Files to create:**
- `frontend/app/login/page.tsx`

**Time estimate**: 2-3 hours

#### 11.5 Create Register Page
- [ ] Create register page (`/register`)
  - Email, password, role selection form
  - Error handling
  - Redirect to login on success

**Files to create:**
- `frontend/app/register/page.tsx`

**Time estimate**: 2-3 hours

#### 11.6 Create Protected Route Wrapper
- [ ] Create `ProtectedRoute` component
  - Check auth state
  - Redirect to login if not authenticated
  - Check role if needed

**Files to create:**
- `frontend/components/ProtectedRoute.tsx`

**Time estimate**: 2 hours

#### 11.7 Update Layout
- [ ] Update root layout
  - Wrap app with AuthProvider
  - Add navigation (if logged in, show logout)

**Files to modify:**
- `frontend/app/layout.tsx`

**Time estimate**: 1-2 hours

**Deliverable**: Users can register, login, and auth state is managed throughout app

---

## Phase 12: Frontend - User Dashboard
**Duration**: 3-4 days  
**Goal**: Build user wallet view, top-up, and purchase pages

### Tasks

#### 12.1 Create Wallet API Functions
- [ ] Create wallet API functions:
  - `getWallets()`
  - `getWallet(walletId)`

**Files to create:**
- `frontend/lib/api/wallet.ts`

**Time estimate**: 1 hour

#### 12.2 Create Wallet Dashboard Page
- [ ] Create wallet dashboard (`/dashboard` or `/`)
  - Display all wallets (Reward Points, MGC, UGC per game)
  - Show balances
  - Link to top-up and purchase actions

**Files to create:**
- `frontend/app/dashboard/page.tsx`
- `frontend/components/WalletCard.tsx`

**Time estimate**: 4-5 hours

#### 12.3 Create Top-up API Function
- [ ] Create top-up API function:
  - `topup(pointsAmount, idempotencyKey)`

**Files to create:**
- `frontend/lib/api/topup.ts`

**Time estimate**: 1 hour

#### 12.4 Create Top-up Page
- [ ] Create top-up page (`/topup`)
  - Form: points amount or MGC amount
  - Generate idempotency key (UUID)
  - Show rate preview
  - Submit and show result
  - Error handling

**Files to create:**
- `frontend/app/topup/page.tsx`
- `frontend/components/TopupForm.tsx`

**Time estimate**: 4-5 hours

#### 12.5 Create Purchase API Function
- [ ] Create purchase API function:
  - `purchase(gameId, mgcAmount, idempotencyKey)`

**Files to create:**
- `frontend/lib/api/purchase.ts`

**Time estimate**: 1 hour

#### 12.6 Create Purchase Page
- [ ] Create purchase page (`/games/[gameId]/purchase`)
  - Form: MGC amount or UGC amount
  - Generate idempotency key
  - Show rate preview
  - Show game info
  - Submit and show result
  - Error handling

**Files to create:**
- `frontend/app/games/[gameId]/purchase/page.tsx`
- `frontend/components/PurchaseForm.tsx`

**Time estimate**: 4-5 hours

#### 12.7 Create Transaction History
- [ ] Create transaction history page (`/transactions`)
  - List topups and purchases
  - Show details (amounts, rates, timestamps)
  - Filter by type

**Files to create:**
- `frontend/app/transactions/page.tsx`
- `frontend/components/TransactionList.tsx`

**Time estimate**: 3-4 hours

#### 12.8 Create Games List Page
- [ ] Create games list page (`/games`)
  - Display available games
  - Link to purchase page for each game

**Files to create:**
- `frontend/app/games/page.tsx`
- `frontend/components/GameCard.tsx`

**Time estimate**: 2-3 hours

**Deliverable**: Users can view wallets, top-up Points→MGC, purchase MGC→UGC, view transaction history

---

## Phase 13: Frontend - Developer Dashboard
**Duration**: 2-3 days  
**Goal**: Build developer receivables + settlement UI, plus developer game creation and rate proposals

### Tasks

#### 13.1 Create Developer API Functions
- [ ] Create developer API functions:
  - `getReceivables(status)`
  - `requestSettlement()`
  - `getSettlementBatches()`
  - `getSettlementBatch(batchId)`
  - `createGame(name, settlementCurrency)` (developer submits for approval)
  - `createGameRate(gameId, ugcPerMgc)` (developer submits for approval)

**Files to create:**
- `frontend/lib/api/developer.ts`

**Time estimate**: 2 hours

#### 13.2 Create Developer Dashboard Page
- [ ] Create developer dashboard (`/developer/dashboard`)
  - Show total unsettled receivables
  - List unsettled receivables
  - Button to request settlement
  - List settlement batches with status
  - Create/add games (pending admin approval)
  - Propose MGC→UGC rates per game (pending admin approval)

**Files to create:**
- `frontend/app/developer/dashboard/page.tsx`
- `frontend/components/ReceivableCard.tsx`
- `frontend/components/SettlementBatchCard.tsx`
  - `frontend/components/DeveloperGameForm.tsx`
  - `frontend/components/RateProposalForm.tsx`

**Time estimate**: 5-6 hours

#### 13.3 Create Settlement Details Page
- [ ] Create settlement details page (`/developer/settlements/[batchId]`)
  - Show batch details (status, amount, currency, timestamps)
  - List receivables in batch
  - Show failure reason if failed

**Files to create:**
- `frontend/app/developer/settlements/[batchId]/page.tsx`

**Time estimate**: 3-4 hours

**Deliverable**: Developers can view receivables, trigger settlements, view settlement history, and submit games/rates for approval

---

## Phase 14: Frontend - Admin Panel
**Duration**: 2-3 days  
**Goal**: Build admin UI for approvals, rates, games, and FX management

### Tasks

#### 14.1 Create Admin API Functions
- [ ] Create admin API functions:
  - `createPointsMgcRate(pointsPerMgc)`
  - `createMgcUgcRate(gameId, ugcPerMgc)`
  - `approveGame(gameId)` / `rejectGame(gameId)`
  - `approveGameRate(rateId)` / `rejectGameRate(rateId)`
  - `approveDeveloper(developerId)` / `rejectDeveloper(developerId)`
  - `listDeveloperAccounts()` (with unsettled totals)
  - `listAllTransactions()` (topups, purchases, receivables)
  - `refreshFxRates()`
  - `getFxWindows()`
  - `createGame(developerId, name, settlementCurrency)` (admin-verified path)
  - `createDeveloper(name, settlementCurrency, bankAccountRef)` (admin-verified path)

**Files to create:**
- `frontend/lib/api/admin.ts`

**Time estimate**: 2-3 hours

#### 14.2 Create Admin Dashboard Page
- [ ] Create admin dashboard (`/admin/dashboard`)
  - Overview of system stats
  - Quick actions (refresh FX, create rate, etc.)
  - Pending approvals (developers, games, rate changes)

**Files to create:**
- `frontend/app/admin/dashboard/page.tsx`

**Time estimate**: 2-3 hours

#### 14.3 Create Rate Management Page
- [ ] Create rate management page (`/admin/rates`)
  - Form to create Points→MGC rate
  - Form to create MGC→UGC rate (with game selector)
  - List active rates

**Files to create:**
- `frontend/app/admin/rates/page.tsx`
- `frontend/components/RateForm.tsx`

**Time estimate**: 4-5 hours

#### 14.4 Create FX Management Page
- [ ] Create FX management page (`/admin/fx`)
  - Button to manually refresh FX rates
  - List FX windows with status
  - Show current active window

**Files to create:**
- `frontend/app/admin/fx/page.tsx`

**Time estimate**: 3-4 hours

#### 14.5 Create Game Management Page
- [ ] Create game management page (`/admin/games`)
  - Form to create new game
  - List all games with approval status
  - Approve/reject pending games
  - Activate/deactivate games

**Files to create:**
- `frontend/app/admin/games/page.tsx`
- `frontend/components/GameForm.tsx`
  - `frontend/components/GameApprovalTable.tsx`

**Time estimate**: 3-4 hours

#### 14.6 Create Developer Management Page
- [ ] Create developer management page (`/admin/developers`)
  - Form to create new developer
  - List all developers with approval status
  - Approve/reject developers
  - Show unsettled totals per developer

**Files to create:**
- `frontend/app/admin/developers/page.tsx`
- `frontend/components/DeveloperForm.tsx`
  - `frontend/components/DeveloperApprovalTable.tsx`

**Time estimate**: 2-3 hours

**Deliverable**: Admins can approve developers/games/rates, manage rates/FX, and view developer exposure & transactions

---

## Phase 15: Testing & Polish
**Duration**: 3-4 days  
**Goal**: Add tests, error handling, logging, documentation

### Tasks

#### 15.1 Add Integration Tests
- [ ] Test top-up flow end-to-end
- [ ] Test purchase flow end-to-end
- [ ] Test settlement flow end-to-end
- [ ] Test idempotency
- [ ] Test concurrent operations

**Files to create:**
- `backend/src/test/java/com/mgx/integration/`

**Time estimate**: 6-8 hours

#### 15.2 Add Unit Tests
- [ ] Test service layer methods
- [ ] Test utility functions
- [ ] Test exception handling

**Files to create:**
- `backend/src/test/java/com/mgx/*/service/*Test.java`

**Time estimate**: 4-6 hours

#### 15.3 Add Structured Logging
- [ ] Add correlation IDs to requests
- [ ] Log key operations (topup, purchase, settlement)
- [ ] Include domain IDs in logs (topupId, purchaseId, batchId)

**Files to modify:**
- Add logging to all services and controllers

**Time estimate**: 3-4 hours

#### 15.4 Add API Documentation
- [ ] Configure Swagger/OpenAPI
- [ ] Add API annotations to controllers
- [ ] Document request/response DTOs
- [ ] Access at `/swagger-ui.html`

**Time estimate**: 2-3 hours

#### 15.5 Frontend Error Handling
- [ ] Add error boundaries
- [ ] Improve error messages
- [ ] Add loading states
- [ ] Add toast notifications

**Time estimate**: 4-5 hours

#### 15.6 Frontend Styling & UX
- [ ] Improve UI consistency
- [ ] Add loading spinners
- [ ] Add form validation
- [ ] Improve mobile responsiveness

**Time estimate**: 4-6 hours

#### 15.7 Documentation
- [ ] Update README with setup instructions
- [ ] Document API endpoints
- [ ] Document environment variables
- [ ] Add architecture diagrams (if needed)

**Files to create/update:**
- `README.md`
- `backend/README.md`
- `frontend/README.md`

**Time estimate**: 3-4 hours

**Deliverable**: System is tested, well-documented, and production-ready

---

## Timeline Summary

| Phase | Duration | Cumulative |
|-------|----------|------------|
| Phase 0: Infrastructure | 1-2 days | 1-2 days |
| Phase 1: Entities & Repositories | 2-3 days | 3-5 days |
| Phase 2: Authentication | 2-3 days | 5-8 days |
| Phase 3: Common Infrastructure | 1-2 days | 6-10 days |
| Phase 4: Wallet & Ledger | 2 days | 8-12 days |
| Phase 5: Rates Module | 2 days | 10-14 days |
| Phase 6: FX Module | 2-3 days | 12-17 days |
| Phase 7: Top-up Module | 2-3 days | 14-20 days |
| Phase 8: Purchase Module | 3-4 days | 17-24 days |
| Phase 9: Settlement Module | 3-4 days | 20-28 days |
| Phase 10: Admin & Game Management | 1-2 days | 21-30 days |
| Phase 11: Frontend - Auth | 2-3 days | 23-33 days |
| Phase 12: Frontend - User Dashboard | 3-4 days | 26-37 days |
| Phase 13: Frontend - Developer Dashboard | 2-3 days | 28-40 days |
| Phase 14: Frontend - Admin Panel | 2-3 days | 30-43 days |
| Phase 15: Testing & Polish | 3-4 days | 33-47 days |

**Total Estimated Time: 6-8 weeks (assuming full-time development)**

---

## Critical Path & Dependencies

**Must Complete First:**
1. Phase 0 (Infrastructure) - Required for everything
2. Phase 1 (Entities) - Required for all services
3. Phase 2 (Auth) - Required for protected endpoints
4. Phase 3 (Common) - Required for error handling and idempotency

**Can Work in Parallel:**
- Phase 4 (Wallet) and Phase 5 (Rates) can be done in parallel
- Phase 6 (FX) can start after Phase 5
- Phase 7 (Top-up) requires Phase 4, 5, and 3
- Phase 8 (Purchase) requires Phase 4, 5, 6, and 3
- Phase 9 (Settlement) requires Phase 8
- Frontend phases (11-14) can start after backend APIs are ready

**Recommended Order:**
1. Complete Phases 0-3 (Foundation)
2. Complete Phases 4-6 (Core Services)
3. Complete Phases 7-8 (User Flows)
4. Complete Phase 9 (Settlement)
5. Complete Phase 10 (Admin APIs)
6. Complete Phases 11-14 (Frontend)
7. Complete Phase 15 (Polish)

---

## Risk Mitigation

**High-Risk Areas:**
1. **Atomic Transactions** (Top-up, Purchase) - Test thoroughly, use @Transactional
2. **Idempotency** - Critical for correctness, test edge cases
3. **Settlement Reservation** - Use FOR UPDATE SKIP LOCKED, test concurrency
4. **FX Conversion** - Verify USD-base formula, test edge cases
5. **Optimistic Locking** - Handle version conflicts gracefully

**Mitigation Strategies:**
- Write tests early for critical paths
- Use database transactions correctly
- Test concurrent operations
- Add logging for debugging
- Review code for race conditions

---

## Success Criteria

**Backend:**
- ✅ All endpoints implemented and working
- ✅ Idempotency enforced for top-up and purchase
- ✅ Atomic transactions for all money operations
- ✅ Settlement reservation prevents double payouts
- ✅ FX rates refresh twice daily
- ✅ All invariants preserved

**Frontend:**
- ✅ Users can register, login, top-up, purchase
- ✅ Developers can view receivables and trigger settlements
- ✅ Admins can manage rates, games, FX
- ✅ Error handling and loading states
- ✅ Responsive design

**Quality:**
- ✅ Integration tests pass
- ✅ API documentation complete
- ✅ README with setup instructions
- ✅ No critical bugs

---

## Notes

- This plan assumes you're working full-time (8 hours/day)
- Adjust timeline based on your experience level
- Some phases can be done in parallel (e.g., frontend while backend is being built)
- Prioritize core user flows (top-up, purchase) before admin features
- Test as you go, don't wait until Phase 15
- Use git branches for features, commit frequently

Good luck with the development! 🚀
