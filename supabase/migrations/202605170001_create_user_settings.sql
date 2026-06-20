create table if not exists public.user_settings (
  user_id uuid primary key references auth.users(id) on delete cascade,
  ai_provider text not null default 'gemini',
  ai_model text not null default '',
  ai_api_key text not null default '',
  ai_ollama_url text not null default 'http://localhost:11434',
  theme text not null default 'system',
  accent_color text not null default '#1e88e5',
  language text not null default 'es',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

alter table public.user_settings enable row level security;

create policy "Users can read own settings"
  on public.user_settings for select
  to authenticated using (auth.uid() = user_id);

create policy "Users can insert own settings"
  on public.user_settings for insert
  to authenticated with check (auth.uid() = user_id);

create policy "Users can update own settings"
  on public.user_settings for update
  to authenticated using (auth.uid() = user_id);
