create extension if not exists "pgcrypto";

create table if not exists public.products (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  local_id integer,
  title text not null,
  url text not null default '',
  brand text not null default '',
  image_url text not null default '',
  price numeric(12, 2) not null default 0,
  currency text not null default '$',
  category text not null default 'Otros',
  priority text not null default 'Media',
  status text not null default 'Por revisar',
  reminder_date timestamptz,
  rating numeric(3, 2) not null default 0,
  summary_md text not null default '',
  notes text not null default '',
  source_store text not null default '',
  comparison_group_id text,
  quality integer not null default 3,
  pros text not null default '',
  contras text not null default '',
  score integer not null default 70,
  price_alert_enabled boolean not null default false,
  price_alert_threshold numeric(12, 2) not null default 0,
  price_history text not null default '',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists products_user_id_created_at_idx
  on public.products (user_id, created_at desc);

alter table public.products enable row level security;

create policy "Users can read their own products"
  on public.products
  for select
  to authenticated
  using (auth.uid() = user_id);

create policy "Users can insert their own products"
  on public.products
  for insert
  to authenticated
  with check (auth.uid() = user_id);

create policy "Users can update their own products"
  on public.products
  for update
  to authenticated
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

create policy "Users can delete their own products"
  on public.products
  for delete
  to authenticated
  using (auth.uid() = user_id);
