-- Country isolation, phone identity, API keys, OTP, approvals

-- Supported countries (seed minimal set)
CREATE TABLE IF NOT EXISTS mgx_supported_countries (
  country_code TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE'))
);

INSERT INTO mgx_supported_countries (country_code, name, status)
VALUES
  ('US', 'United States', 'ACTIVE'),
  ('IN', 'India', 'ACTIVE')
ON CONFLICT (country_code) DO NOTHING;

-- Users: phone + country + verification
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_number TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS country_code TEXT NOT NULL DEFAULT 'US';
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_verified_at TIMESTAMPTZ NULL;
ALTER TABLE users
  ADD CONSTRAINT users_country_fk
  FOREIGN KEY (country_code) REFERENCES mgx_supported_countries(country_code);
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_phone ON users(phone_number);

-- Wallets: country scoped
ALTER TABLE wallets ADD COLUMN IF NOT EXISTS country_code TEXT NOT NULL DEFAULT 'US';
ALTER TABLE wallets
  ADD CONSTRAINT wallets_country_fk
  FOREIGN KEY (country_code) REFERENCES mgx_supported_countries(country_code);

DROP INDEX IF EXISTS ux_wallet_user_type_nullgame;
DROP INDEX IF EXISTS ux_wallet_user_type_game;
DROP INDEX IF EXISTS idx_wallet_user;

CREATE UNIQUE INDEX ux_wallet_user_type_country_nullgame
ON wallets(user_id, type, country_code)
WHERE game_id IS NULL;

CREATE UNIQUE INDEX ux_wallet_user_type_country_game
ON wallets(user_id, type, game_id, country_code)
WHERE game_id IS NOT NULL;

CREATE INDEX idx_wallet_user_country ON wallets(user_id, country_code);

-- Game allowed countries
CREATE TABLE IF NOT EXISTS game_allowed_countries (
  game_id UUID NOT NULL REFERENCES games(id),
  country_code TEXT NOT NULL REFERENCES mgx_supported_countries(country_code),
  PRIMARY KEY (game_id, country_code)
);

INSERT INTO game_allowed_countries (game_id, country_code)
SELECT id, 'US' FROM games
ON CONFLICT DO NOTHING;

-- API keys for private endpoints
CREATE TABLE IF NOT EXISTS api_keys (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  key_hash TEXT NOT NULL UNIQUE,
  owner_name TEXT NOT NULL,
  scopes TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','REVOKED')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  revoked_at TIMESTAMPTZ NULL
);

-- Bank links
CREATE TABLE IF NOT EXISTS bank_links (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  bank_ref TEXT NOT NULL,
  phone_number TEXT NOT NULL,
  verified_at TIMESTAMPTZ NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- OTP sessions
CREATE TABLE IF NOT EXISTS otp_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  phone_number TEXT NOT NULL,
  purpose TEXT NOT NULL CHECK (purpose IN ('REGISTER','BANK_PURCHASE','PHONE_CHANGE')),
  code_hash TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','VERIFIED','EXPIRED','FAILED')),
  attempts INT NOT NULL DEFAULT 0,
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Approval requests
CREATE TABLE IF NOT EXISTS approval_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  request_type TEXT NOT NULL CHECK (request_type IN ('DEVELOPER','GAME','RATE','SETTLEMENT')),
  entity_id UUID NOT NULL,
  status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED')),
  requested_by UUID NULL REFERENCES users(id),
  requested_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  reviewed_by UUID NULL REFERENCES users(id),
  reviewed_at TIMESTAMPTZ NULL,
  rejection_reason TEXT NULL
);
