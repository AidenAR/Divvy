-- Multi-Currency Support Migration
-- Creates NEW functions with _v2 suffix. Original functions are left untouched.
-- The `expenses` table already has a `currency` column.
-- The `group_expenses_with_splits` view already includes `currency` — no changes needed.

-- 1. net_balances_v2: like net_balances but groups by (user_id, currency)
--    Changes vs original: added `e.currency` to SELECT and GROUP BY in expense subqueries,
--    hardcoded 'USD' for settlements (settlements table has no currency column).
CREATE OR REPLACE FUNCTION net_balances_v2(p_group_id uuid)
RETURNS TABLE(user_id uuid, currency text, balance_cents bigint) AS $$
BEGIN
  RETURN QUERY
  SELECT
    sub.user_id,
    sub.currency,
    SUM(sub.balance_cents)::bigint AS balance_cents
  FROM (
    -- Splits where current user paid → effective debtors owe current user
    select
        COALESCE(es.is_covered_by, es.user_id) as user_id,
        COALESCE(e.currency, 'USD')::text as currency,
        sum(es.amount_cents)::bigint as balance_cents
    from public.expenses e
    join public.expense_splits es on es.expense_id = e.id
    where e.group_id        = p_group_id
      and e.paid_by_user_id = auth.uid()
      and COALESCE(es.is_covered_by, es.user_id) <> auth.uid()
    group by COALESCE(es.is_covered_by, es.user_id), COALESCE(e.currency, 'USD')

    union all

    -- Splits where another member paid → current user owes them (as effective debtor)
    select
        e.paid_by_user_id                 as user_id,
        COALESCE(e.currency, 'USD')::text as currency,
        -sum(es.amount_cents)::bigint     as balance_cents
    from public.expenses e
    join public.expense_splits es on es.expense_id = e.id
    where e.group_id        = p_group_id
      and e.paid_by_user_id <> auth.uid()
      and COALESCE(es.is_covered_by, es.user_id) = auth.uid()
    group by e.paid_by_user_id, COALESCE(e.currency, 'USD')

    union all

    -- Settlements paid BY current user TO others
    select
        payee_id                         as user_id,
        'USD'::text                      as currency,
        sum(amount_cents)::bigint        as balance_cents
    from public.settlements
    where group_id = p_group_id
      and payer_id = auth.uid()
    group by payee_id

    union all

    -- Settlements paid TO current user BY others
    select
        payer_id                         as user_id,
        'USD'::text                      as currency,
        -sum(amount_cents)::bigint       as balance_cents
    from public.settlements
    where group_id = p_group_id
      and payee_id = auth.uid()
    group by payer_id
  ) sub
  GROUP BY sub.user_id, sub.currency;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2. get_my_groups_summary_v2: like get_my_groups_summary but adds `balances` JSONB column
--    Changes vs original: added `balances` JSONB column containing per-currency breakdown.
--    `balance_cents` is kept for backward compat (sum across all currencies).
CREATE OR REPLACE FUNCTION get_my_groups_summary_v2()
RETURNS TABLE(
  id uuid,
  name text,
  icon text,
  created_by uuid,
  created_at timestamptz,
  member_count bigint,
  balance_cents bigint,
  balances jsonb
) AS $$
BEGIN
  RETURN QUERY
  SELECT
    g.id,
    g.name,
    g.icon,
    g.created_by,
    g.created_at,
    (SELECT COUNT(*) FROM group_members gm2 WHERE gm2.group_id = g.id)
      AS member_count,
    COALESCE(
      (SELECT SUM(
        CASE
          WHEN e.paid_by_user_id = auth.uid()
          THEN e.amount_cents - COALESCE(
            (SELECT es.amount_cents
             FROM expense_splits es
             WHERE es.expense_id = e.id AND es.user_id = auth.uid()),
            0)
          ELSE -COALESCE(
            (SELECT es.amount_cents
             FROM expense_splits es
             WHERE es.expense_id = e.id AND es.user_id = auth.uid()),
            0)
        END
      ) FROM expenses e WHERE e.group_id = g.id),
      0
    ) AS balance_cents,
    COALESCE((
      SELECT jsonb_agg(jsonb_build_object('currency', agg.currency, 'balance_cents', agg.total_cents))
      FROM (
        SELECT nb.currency, SUM(nb.balance_cents)::bigint AS total_cents
        FROM net_balances_v2(g.id) nb
        GROUP BY nb.currency
      ) agg
    ), '[]'::jsonb) AS balances
  FROM groups g
  JOIN group_members gm ON gm.group_id = g.id AND gm.user_id = auth.uid();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 3. get_global_activity_feed_v2: like get_global_activity_feed but includes `currency`
--    Changes vs original: added `e.currency` to SELECT and RETURNS TABLE,
--    added 'USD' fallback for MEMBER_JOINED events.
CREATE OR REPLACE FUNCTION get_global_activity_feed_v2(p_limit int DEFAULT 50)
RETURNS TABLE(
  id text,
  activity_type text,
  group_id uuid,
  group_name text,
  group_icon text,
  title text,
  amount_cents bigint,
  actor_id uuid,
  actor_name text,
  actor_avatar_url text,
  created_at timestamptz,
  currency text
) AS $$
DECLARE
  v_user_id UUID := auth.uid();
BEGIN
  RETURN QUERY

  -- Expenses & Settlements in user's groups
  SELECT
    e.id::TEXT,
    CASE WHEN e.split_method = 'SETTLEMENT' THEN 'SETTLEMENT' ELSE 'EXPENSE' END,
    e.group_id,
    g.name,
    g.icon,
    e.merchant,
    e.amount_cents,
    e.paid_by_user_id,
    COALESCE(p.first_name || ' ' || p.last_name, 'Unknown'),
    NULL::TEXT,
    e.created_at,
    COALESCE(e.currency, 'USD')::TEXT
  FROM expenses e
  JOIN groups g ON g.id = e.group_id
  JOIN group_members gm ON gm.group_id = e.group_id AND gm.user_id = v_user_id
  LEFT JOIN profiles p ON p.id = e.paid_by_user_id

  UNION ALL

  -- Group join events (anyone joining groups the user is in)
  SELECT
    (gm2.group_id::TEXT || '_' || gm2.user_id::TEXT),
    'MEMBER_JOINED',
    gm2.group_id,
    g2.name,
    g2.icon,
    'joined the group',
    0::BIGINT,
    gm2.user_id,
    COALESCE(p2.first_name || ' ' || p2.last_name, 'Unknown'),
    NULL::TEXT,
    gm2.joined_at,
    'USD'::TEXT
  FROM group_members gm2
  JOIN groups g2 ON g2.id = gm2.group_id
  JOIN group_members my_membership ON my_membership.group_id = gm2.group_id
    AND my_membership.user_id = v_user_id
  LEFT JOIN profiles p2 ON p2.id = gm2.user_id

  ORDER BY created_at DESC
  LIMIT p_limit;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
