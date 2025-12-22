# Demo Flows (Clean Commands)


## Flow A - Admin setup (developer + game + rates + FX)

1) Admin login (get token):
```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"admin1234"}'
```

2) Create developer:
```bash
curl -X POST http://localhost:8081/v1/admin/developers \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Acme Games","settlementCurrency":"USD","bankAccountRef":"BANK-ACME-001"}'
```

3) Create game:
```bash
curl -X POST http://localhost:8081/v1/admin/games \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"developerId":"<DEV_ID>","name":"SpaceRacer","settlementCurrency":"USD"}'
```

4) Create Points->MGC rate:
```bash
curl -X POST http://localhost:8081/v1/admin/rates/points-mgc \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"pointsPerMgc":100}'
```

5) Create MGC->UGC rate:
```bash
curl -X POST http://localhost:8081/v1/admin/rates/mgc-ugc \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"gameId":"<GAME_ID>","ugcPerMgc":10}'
```

6) Refresh FX window:
```bash
curl -X POST http://localhost:8081/v1/admin/fx/refresh \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

## Flow B - User top-up and purchase

1) Register user (get token):
```bash
curl -X POST http://localhost:8081/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo.user@example.com","password":"test1234","role":"USER"}'
```

2) Get user id:
```bash
curl http://localhost:8081/v1/auth/me \
  -H "Authorization: Bearer <USER_TOKEN>"
```

3) Seed reward points:
```bash
psql -h localhost -U mgx_user -d mgx_db -c \
"UPDATE wallets SET balance = 10000 WHERE user_id = '<USER_ID>' AND type = 'REWARD_POINTS';"
```

4) Top-up points -> MGC:
```bash
curl -X POST http://localhost:8081/v1/topups \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -H "Idempotency-Key: topup-demo-001" \
  -H "Content-Type: application/json" \
  -d '{"pointsAmount":5000}'
```

5) Purchase UGC:
```bash
curl -X POST http://localhost:8081/v1/purchases \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -H "Idempotency-Key: purchase-demo-001" \
  -H "Content-Type: application/json" \
  -d '{"gameId":"<GAME_ID>","mgcAmount":10}'
```

## Flow C - Developer settlement

1) Register developer user (get token):
```bash
curl -X POST http://localhost:8081/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo.dev@example.com","password":"dev1234","role":"DEVELOPER"}'
```

2) Request settlement:
```bash
curl -X POST http://localhost:8081/v1/developer/settlements/request \
  -H "Authorization: Bearer <DEV_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"developerId":"<DEV_ID>"}'
```

3) Check batches:
```bash
curl "http://localhost:8081/v1/developer/settlements?developerId=<DEV_ID>" \
  -H "Authorization: Bearer <DEV_TOKEN>"
```

4) Check receivables:
```bash
curl "http://localhost:8081/v1/developer/receivables?developerId=<DEV_ID>&status=SETTLED" \
  -H "Authorization: Bearer <DEV_TOKEN>"
```
