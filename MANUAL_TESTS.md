# Manual Tests (Phases 0-7)

This file collects terminal commands used to manually verify the backend through Phase 7.

## Phase 0 - Infrastructure

### Redis (optional for basic compile, required for caching/idempotency)
Start Redis (Homebrew):
```bash
brew services start redis
```

Or Docker:
```bash
docker run --name mgx-redis -p 6379:6379 -d redis:7
```

### FX Mock (port 8090)
```bash
./backend/mvnw -f services/fx-mock/pom.xml spring-boot:run
```

Test:
```bash
curl http://localhost:8090/fx/rates
```

### Bank Mock (port 8091)
```bash
./backend/mvnw -f services/bank-mock/pom.xml spring-boot:run
```

Test:
```bash
curl -X POST http://localhost:8091/payouts \
  -H 'Content-Type: application/json' \
  -d '{"amount":100,"currency":"USD"}'
```

## Phase 1 - Entities/Repositories

Compile:
```bash
./backend/mvnw -f backend/pom.xml -DskipTests compile
```

## Phase 2 - Auth

Start backend:
```bash
./backend/mvnw -f backend/pom.xml spring-boot:run
```

Register user:
```bash
curl -X POST http://localhost:8081/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"test1234","role":"USER"}'
```

Login:
```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"test1234"}'
```

Me:
```bash
curl http://localhost:8081/v1/auth/me \
  -H 'Authorization: Bearer <TOKEN>'
```

## Phase 4 - Wallets/Ledger

List wallets:
```bash
curl http://localhost:8081/v1/wallets \
  -H 'Authorization: Bearer <TOKEN>'
```

Fetch one wallet:
```bash
curl http://localhost:8081/v1/wallets/<WALLET_ID> \
  -H 'Authorization: Bearer <TOKEN>'
```

## Phase 5 - Rates (Admin)

Register admin:
```bash
curl -X POST http://localhost:8081/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"admin1234","role":"ADMIN"}'
```

Create points->MGC rate:
```bash
curl -X POST http://localhost:8081/v1/admin/rates/points-mgc \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"pointsPerMgc":1000}'
```

Create dev + game (DB only, for MGC->UGC rate):
```bash
psql -h localhost -U mgx_user -d mgx_db -c \
"INSERT INTO developers (name, settlement_currency, bank_account_ref, status) VALUES ('Test Dev','USD','BANK-TEST','ACTIVE') RETURNING id;"
```

```bash
psql -h localhost -U mgx_user -d mgx_db -c \
"INSERT INTO games (developer_id, name, status, settlement_currency) VALUES ('<DEV_ID>','Test Game','ACTIVE','USD') RETURNING id;"
```

Create MGC->UGC rate:
```bash
curl -X POST http://localhost:8081/v1/admin/rates/mgc-ugc \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"gameId":"<GAME_ID>","ugcPerMgc":10}'
```

## Phase 6 - FX

Refresh FX (admin):
```bash
curl -X POST http://localhost:8081/v1/admin/fx/refresh \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

List FX windows:
```bash
curl http://localhost:8081/v1/admin/fx/windows \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

Current FX window + rates:
```bash
curl http://localhost:8081/v1/admin/fx/current \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

## Phase 7 - Top-up

Ensure user has points (DB):
```bash
psql -h localhost -U mgx_user -d mgx_db -c \
"UPDATE wallets SET balance = balance + 10000 WHERE user_id = '<USER_ID>' AND type = 'REWARD_POINTS';"
```

Top-up by points:
```bash
curl -X POST http://localhost:8081/v1/topups \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -H "Idempotency-Key: topup-001" \
  -H "Content-Type: application/json" \
  -d '{"pointsAmount":5000}'
```

Check MGC balance:
```bash
psql -h localhost -U mgx_user -d mgx_db -c \
"SELECT id, balance FROM wallets WHERE user_id = '<USER_ID>' AND type = 'MGC';"
```

## Phase 8 - Purchase

Get user id from token:
```bash
curl http://localhost:8081/v1/auth/me \
  -H "Authorization: Bearer <USER_TOKEN>"
```

List games:
```bash
psql -h localhost -U mgx_user -d mgx_db -c \
"SELECT id, name, developer_id, settlement_currency FROM games;"
```

Check active MGC->UGC rate:
```bash
psql -h localhost -U mgx_user -d mgx_db -c \
"SELECT game_id, ugc_per_mgc, active_from, active_to FROM rate_mgc_ugc WHERE active_to IS NULL OR active_to > NOW() ORDER BY active_from DESC;"
```

Purchase by MGC:
```bash
curl -X POST http://localhost:8081/v1/purchases \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -H "Idempotency-Key: purchase-001" \
  -H "Content-Type: application/json" \
  -d '{"gameId":"<GAME_ID>","mgcAmount":10}'
```

Check UGC wallet for a game:
```bash
psql -h localhost -U mgx_user -d mgx_db -c \
"SELECT id, balance FROM wallets WHERE user_id = '<USER_ID>' AND type = 'UGC' AND game_id = '<GAME_ID>';"
```
