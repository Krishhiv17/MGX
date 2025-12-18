-- MGX Seed Data (dev only)
-- Creates: 1 admin, 1 developer user, 1 normal user,
--          1 developer entity, 1 game, wallets, rates, one FX window (USD base).

-- Fixed UUIDs so references are stable
-- USERS
INSERT INTO users (id, email, password_hash, role)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'admin@mgx.local',  '$2a$10$DUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMY', 'ADMIN'),
  ('22222222-2222-2222-2222-222222222222', 'dev@mgx.local',    '$2a$10$DUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMY', 'DEVELOPER'),
  ('33333333-3333-3333-3333-333333333333', 'user@mgx.local',   '$2a$10$DUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMYDUMMY', 'USER')
ON CONFLICT (email) DO NOTHING;

-- DEVELOPER
INSERT INTO developers (id, name, settlement_currency, bank_account_ref, status)
VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Demo Game Studio', 'USD', 'BANK_REF_DEMO_USD', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- GAME
INSERT INTO games (id, developer_id, name, status, settlement_currency)
VALUES
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'SpaceRacer', 'ACTIVE', 'USD')
ON CONFLICT DO NOTHING;

-- WALLETS for user@mgx.local
-- Reward points wallet (NULL game_id)
INSERT INTO wallets (id, user_id, type, game_id, balance)
VALUES ('44444444-4444-4444-4444-444444444444', '33333333-3333-3333-3333-333333333333', 'REWARD_POINTS', NULL, 100000)
ON CONFLICT DO NOTHING;

-- MGC wallet (NULL game_id)
INSERT INTO wallets (id, user_id, type, game_id, balance)
VALUES ('55555555-5555-5555-5555-555555555555', '33333333-3333-3333-3333-333333333333', 'MGC', NULL, 0)
ON CONFLICT DO NOTHING;

-- UGC wallet for SpaceRacer (non-null game_id)
INSERT INTO wallets (id, user_id, type, game_id, balance)
VALUES ('66666666-6666-6666-6666-666666666666', '33333333-3333-3333-3333-333333333333', 'UGC', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 0)
ON CONFLICT DO NOTHING;

-- RATES
-- Points -> MGC: 100 points = 1 MGC
INSERT INTO rate_points_mgc (id, points_per_mgc, active_from, active_to, created_by)
VALUES
  ('77777777-7777-7777-7777-777777777777', 100, NOW(), NULL, '11111111-1111-1111-1111-111111111111')
ON CONFLICT DO NOTHING;

-- MGC -> UGC for SpaceRacer: 1 MGC = 50 UGC
INSERT INTO rate_mgc_ugc (id, game_id, ugc_per_mgc, active_from, active_to, created_by)
VALUES
  ('88888888-8888-8888-8888-888888888888', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 50, NOW(), NULL, '11111111-1111-1111-1111-111111111111')
ON CONFLICT DO NOTHING;

-- FX WINDOW (USD base) - optional for now but useful later
INSERT INTO fx_rate_windows (id, provider, fetched_at, valid_from, valid_to, status)
VALUES
  ('99999999-9999-9999-9999-999999999999', 'LOCAL_MOCK', NOW(), NOW(), NOW() + INTERVAL '12 hours', 'SUCCESS')
ON CONFLICT DO NOTHING;

-- USD->USD = 1
INSERT INTO fx_rates (window_id, base_currency, quote_currency, rate)
VALUES ('99999999-9999-9999-9999-999999999999', 'USD', 'USD', 1)
ON CONFLICT DO NOTHING;

-- USD->INR example (optional)
INSERT INTO fx_rates (window_id, base_currency, quote_currency, rate)
VALUES ('99999999-9999-9999-9999-999999999999', 'USD', 'INR', 83.25)
ON CONFLICT DO NOTHING;
