-- Run this in the Supabase SQL editor (or add as a migration file)

-- 1. Store the Edge Function URL and service role key as DB settings
--    (set these once via: ALTER DATABASE postgres SET "app.settings.edge_function_url" = '...')
--    Or hard-code the URL below if you prefer.

-- 2. Enable the pg_net extension (required for HTTP calls from Postgres)
create extension if not exists pg_net;

-- 3. Function that fires the Edge Function
create or replace function notify_on_expense_insert()
returns trigger language plpgsql as $$
begin
  perform net.http_post(
    url     := current_setting('app.settings.edge_function_url') || '/notify-on-expense',
    headers := jsonb_build_object(
      'Content-Type',  'application/json',
      'Authorization', 'Bearer ' || current_setting('app.settings.service_role_key')
    ),
    body    := row_to_json(new)::text
  );
  return new;
end;
$$;

-- 4. Attach trigger to the expenses table
drop trigger if exists on_expense_inserted on expenses;

create trigger on_expense_inserted
  after insert on expenses
  for each row
  execute function notify_on_expense_insert();
