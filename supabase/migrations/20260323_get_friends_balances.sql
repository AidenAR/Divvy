-- Migration: Create RPC function for fetching friends with per-group balances
-- Combines get_friends_with_groups() logic with net_balances_v2() via LEFT JOIN LATERAL.
-- Returns one row per (friend, group, currency) tuple.

CREATE OR REPLACE FUNCTION get_friends_balances()
RETURNS TABLE(
  user_id uuid,
  first_name text,
  last_name text,
  email text,
  phone text,
  group_id uuid,
  group_name text,
  group_icon text,
  currency text,
  balance_cents bigint
) AS $$
BEGIN
  RETURN QUERY
  SELECT
    fg.user_id,
    fg.first_name,
    fg.last_name,
    fg.email,
    fg.phone,
    fg.group_id,
    fg.group_name,
    fg.group_icon,
    COALESCE(nb.currency, 'USD')::text,
    COALESCE(nb.balance_cents, 0)::bigint
  FROM (
    SELECT DISTINCT p.id AS user_id, p.first_name, p.last_name, p.email, p.phone,
           g.id AS group_id, g.name AS group_name, g.icon AS group_icon
    FROM group_members gm
    JOIN group_members my_gm ON gm.group_id = my_gm.group_id
    JOIN profiles p ON p.id = gm.user_id
    JOIN groups g ON g.id = gm.group_id
    WHERE my_gm.user_id = auth.uid()
      AND gm.user_id != auth.uid()
  ) fg
  LEFT JOIN LATERAL (
    SELECT nb2.user_id, nb2.currency, nb2.balance_cents
    FROM net_balances_v2(fg.group_id) nb2
    WHERE nb2.user_id = fg.user_id
  ) nb ON true;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
