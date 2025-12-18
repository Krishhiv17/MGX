# MGX Agent Brief

## What this repo is
MGX (Mastercard Game Exchange): localhost-only platform where users convert Bank Reward Points -> MGC wallet,
spend MGC -> UGC per game, and each purchase creates a developer receivable (UNSETTLED). Developers
trigger settlement on-demand (batch/pull model). FX is cached and refreshed twice daily. Each receivable stores
the FX window snapshot used so settlement is deterministic.

## Tech / constraints
- Localhost only. No AWS/GCP.
- Frontend: Next.js (3000)
- Backend: Spring Boot (8081)
- Postgres: 5432 (source of truth)
- Redis optional for caching/idempotency
- NGINX NOT used.

## Key domain terms
- Reward Points: user loyalty points balance
- MGC: Mastercard Game Coin (stored in MGC wallet)
- UGC: Unique Game Coin per game (UGC wallet is per-game)
- Receivable: amount owed to developer, starts UNSETTLED then RESERVED then SETTLED
- FX Window: USD-base rates valid for a time window; refreshed twice daily

## Invariants to preserve
- All money-changing ops must be atomic (single DB txn): ledger + wallet balances + transaction row.
- Idempotency required for topup and purchase.
- Settlement must reserve receivables before processing to prevent double payouts.
- Never recompute FX at settlement time; use stored fx_window_id + fx_rate_used snapshot.

## Where to look
- docs/ARCHITECTURE.md and docs/System Design.pdf contain the full system design.
- backend/src/main/resources/db/migration has schema + seed.
