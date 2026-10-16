-- Run in Supabase > SQL Editor
create table public.sessions (id uuid primary key default gen_random_uuid(), user_id uuid not null default auth.uid() references auth.users on delete cascade,
  session_type text not null, input_text text not null, ai_output text not null, created_at timestamptz not null default now());
create table public.grades (id uuid primary key default gen_random_uuid(), user_id uuid not null default auth.uid() references auth.users on delete cascade,
  module_name text not null, online_test numeric not null, assignment numeric not null, exam numeric not null, created_at timestamptz not null default now());
alter table public.sessions enable row level security; alter table public.grades enable row level security;
create policy "own sessions" on public.sessions for all using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "own grades" on public.grades for all using (auth.uid() = user_id) with check (auth.uid() = user_id);
create function public.delete_me() returns void language sql security definer as $$ delete from auth.users where id = auth.uid(); $$;
