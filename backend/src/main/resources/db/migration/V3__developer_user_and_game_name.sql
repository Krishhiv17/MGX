-- Add developer user linkage and denormalized game developer name
ALTER TABLE developers
  ADD COLUMN user_id UUID NULL REFERENCES users(id);

ALTER TABLE games
  ADD COLUMN developer_name TEXT NULL;

-- Backfill developer_name for existing games
UPDATE games g
SET developer_name = d.name
FROM developers d
WHERE g.developer_id = d.id;
