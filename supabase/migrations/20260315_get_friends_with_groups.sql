-- Migration: Create RPC function for fetching friends with shared groups
-- Run this in Supabase SQL Editor or as a migration

CREATE OR REPLACE FUNCTION get_friends_with_groups()
RETURNS TABLE(
  user_id uuid, first_name text, last_name text,
  email text, phone text,
  group_id uuid, group_name text, group_icon text
) AS $$
  SELECT DISTINCT p.id, p.first_name, p.last_name, p.email, p.phone,
         g.id, g.name, g.icon
  FROM group_members gm
  JOIN group_members my_gm ON gm.group_id = my_gm.group_id
  JOIN profiles p ON p.id = gm.user_id
  JOIN groups g ON g.id = gm.group_id
  WHERE my_gm.user_id = auth.uid()
    AND gm.user_id != auth.uid()
$$ LANGUAGE sql SECURITY DEFINER STABLE;
