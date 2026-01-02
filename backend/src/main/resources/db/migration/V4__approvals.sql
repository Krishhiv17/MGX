-- Add approval workflow fields for developers, games, and MGC->UGC rates

-- Developers: add approval metadata and update status constraint/default
ALTER TABLE developers ADD COLUMN IF NOT EXISTS approved_by UUID NULL REFERENCES users(id);
ALTER TABLE developers ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ NULL;
ALTER TABLE developers DROP CONSTRAINT IF EXISTS developers_status_check;
ALTER TABLE developers
  ADD CONSTRAINT developers_status_check
  CHECK (status IN ('PENDING_APPROVAL','ACTIVE','REJECTED','INACTIVE'));
ALTER TABLE developers ALTER COLUMN status SET DEFAULT 'PENDING_APPROVAL';

-- Games: add approval metadata and update status constraint/default
ALTER TABLE games ADD COLUMN IF NOT EXISTS approved_by UUID NULL REFERENCES users(id);
ALTER TABLE games ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ NULL;
ALTER TABLE games DROP CONSTRAINT IF EXISTS games_status_check;
ALTER TABLE games
  ADD CONSTRAINT games_status_check
  CHECK (status IN ('PENDING_APPROVAL','ACTIVE','REJECTED','INACTIVE'));
ALTER TABLE games ALTER COLUMN status SET DEFAULT 'PENDING_APPROVAL';

-- Rates: add approval metadata + status
ALTER TABLE rate_mgc_ugc ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'APPROVED';
ALTER TABLE rate_mgc_ugc ADD COLUMN IF NOT EXISTS approved_by UUID NULL REFERENCES users(id);
ALTER TABLE rate_mgc_ugc ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ NULL;
ALTER TABLE rate_mgc_ugc
  ADD CONSTRAINT rate_mgc_ugc_status_check
  CHECK (status IN ('PENDING','APPROVED','REJECTED'));
