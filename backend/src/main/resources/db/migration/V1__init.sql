-- MGX - Initial Schema (PostgreSQL)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- USERS
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  role TEXT NOT NULL CHECK (role IN ('USER','DEVELOPER','ADMIN')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- DEVELOPERS
CREATE TABLE developers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  settlement_currency TEXT NOT NULL,
  bank_account_ref TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- GAMES
CREATE TABLE games (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  developer_id UUID NOT NULL REFERENCES developers(id),
  name TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
  settlement_currency TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_games_developer ON games(developer_id);

-- WALLETS
CREATE TABLE wallets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  type TEXT NOT NULL CHECK (type IN ('REWARD_POINTS','MGC','UGC')),
  game_id UUID NULL REFERENCES games(id),
  balance NUMERIC(38,12) NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
-- Unique wallet constraints (handle NULL game_id correctly)
CREATE UNIQUE INDEX ux_wallet_user_type_nullgame
ON wallets(user_id, type)
WHERE game_id IS NULL;

CREATE UNIQUE INDEX ux_wallet_user_type_game
ON wallets(user_id, type, game_id)
WHERE game_id IS NOT NULL;

CREATE INDEX idx_wallet_user ON wallets(user_id);

-- LEDGER
CREATE TABLE ledger_entries (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  ref_type TEXT NOT NULL CHECK (ref_type IN ('TOPUP','PURCHASE','SETTLEMENT_ADJUSTMENT')),
  ref_id UUID NOT NULL,
  wallet_id UUID NOT NULL REFERENCES wallets(id),
  direction TEXT NOT NULL CHECK (direction IN ('DEBIT','CREDIT')),
  asset_type TEXT NOT NULL CHECK (asset_type IN ('POINTS','MGC','UGC')),
  amount NUMERIC(38,12) NOT NULL CHECK (amount > 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_ledger_wallet_time ON ledger_entries(wallet_id, created_at);

-- RATES
CREATE TABLE rate_points_mgc (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  points_per_mgc NUMERIC(38,12) NOT NULL CHECK (points_per_mgc > 0),
  active_from TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  active_to TIMESTAMPTZ NULL,
  created_by UUID NULL REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE rate_mgc_ugc (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  game_id UUID NOT NULL REFERENCES games(id),
  ugc_per_mgc NUMERIC(38,12) NOT NULL CHECK (ugc_per_mgc > 0),
  active_from TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  active_to TIMESTAMPTZ NULL,
  created_by UUID NULL REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_rate_mgc_ugc_game ON rate_mgc_ugc(game_id, active_from);

-- TOPUPS
CREATE TABLE topups (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  points_debited NUMERIC(38,12) NOT NULL CHECK (points_debited > 0),
  mgc_credited NUMERIC(38,12) NOT NULL CHECK (mgc_credited > 0),
  rate_points_per_mgc_snapshot NUMERIC(38,12) NOT NULL CHECK (rate_points_per_mgc_snapshot > 0),
  rate_id UUID NULL REFERENCES rate_points_mgc(id),
  status TEXT NOT NULL CHECK (status IN ('INITIATED','COMPLETED','FAILED')),
  idempotency_key TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (user_id, idempotency_key)
);
CREATE INDEX idx_topups_user_time ON topups(user_id, created_at);

-- PURCHASES
CREATE TABLE purchases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  game_id UUID NOT NULL REFERENCES games(id),
  mgc_spent NUMERIC(38,12) NOT NULL CHECK (mgc_spent > 0),
  ugc_credited NUMERIC(38,12) NOT NULL CHECK (ugc_credited > 0),
  rate_ugc_per_mgc_snapshot NUMERIC(38,12) NOT NULL CHECK (rate_ugc_per_mgc_snapshot > 0),
  rate_id UUID NULL REFERENCES rate_mgc_ugc(id),
  status TEXT NOT NULL CHECK (status IN ('INITIATED','COMPLETED','FAILED')),
  idempotency_key TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (user_id, idempotency_key)
);
CREATE INDEX idx_purchases_user_time ON purchases(user_id, created_at);
CREATE INDEX idx_purchases_game_time ON purchases(game_id, created_at);

-- FX WINDOWS
CREATE TABLE fx_rate_windows (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  provider TEXT NOT NULL,
  fetched_at TIMESTAMPTZ NOT NULL,
  valid_from TIMESTAMPTZ NOT NULL,
  valid_to TIMESTAMPTZ NOT NULL,
  status TEXT NOT NULL CHECK (status IN ('SUCCESS','FAILED'))
);

CREATE TABLE fx_rates (
  window_id UUID NOT NULL REFERENCES fx_rate_windows(id),
  base_currency TEXT NOT NULL DEFAULT 'USD',
  quote_currency TEXT NOT NULL,
  rate NUMERIC(38,12) NOT NULL CHECK (rate > 0),
  PRIMARY KEY (window_id, quote_currency)
);

-- SETTLEMENT BATCHES
CREATE TABLE settlement_batches (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  developer_id UUID NOT NULL REFERENCES developers(id),
  requested_by UUID NOT NULL REFERENCES users(id),
  status TEXT NOT NULL CHECK (status IN ('REQUESTED','PROCESSING','PAID','FAILED')),
  currency TEXT NOT NULL,
  total_amount NUMERIC(38,12) NOT NULL DEFAULT 0,
  requested_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  processed_at TIMESTAMPTZ NULL,
  failure_reason TEXT NULL
);
CREATE INDEX idx_settlement_batches_dev_time ON settlement_batches(developer_id, requested_at);

-- RECEIVABLES
CREATE TABLE receivables (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  purchase_id UUID NOT NULL REFERENCES purchases(id),
  developer_id UUID NOT NULL REFERENCES developers(id),
  amount_due NUMERIC(38,12) NOT NULL CHECK (amount_due >= 0),
  settlement_currency TEXT NOT NULL,
  fx_window_id UUID NULL REFERENCES fx_rate_windows(id),
  fx_rate_used NUMERIC(38,12) NULL,
  status TEXT NOT NULL CHECK (status IN ('UNSETTLED','RESERVED','SETTLED')),
  reserved_at TIMESTAMPTZ NULL,
  settled_at TIMESTAMPTZ NULL,
  settlement_batch_id UUID NULL REFERENCES settlement_batches(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_receivables_lookup ON receivables(developer_id, status, created_at);
