-- MEDSPACE — الإعداد الأول لقاعدة البيانات (الجزء 1: الحسابات والصلاحيات)
-- الصق هذا كاملًا في Supabase → SQL Editor ثم اضغط Run.
-- غيّر البريد في السطر المحدد قبل التشغيل ليكون بريدك أنت (حساب المدير).

create table if not exists public.app_settings (
  key text primary key,
  value text not null
);
alter table public.app_settings enable row level security;
-- لا توجد سياسات على هذا الجدول: لا يستطيع أحد قراءته أو تعديله من التطبيق.

insert into public.app_settings (key, value)
values ('super_admin_email', 'PUT_YOUR_EMAIL_HERE')   -- <<< غيّر هذا السطر
on conflict (key) do update set value = excluded.value;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text not null default '',
  email text not null default '',
  role text not null default 'student' check (role in ('student','super_admin')),
  status text not null default 'pending' check (status in ('pending','approved','rejected','suspended')),
  created_at timestamptz not null default now()
);
alter table public.profiles enable row level security;

create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
declare admin_email text;
declare is_owner boolean;
begin
  select value into admin_email from public.app_settings where key = 'super_admin_email';
  is_owner := admin_email is not null and lower(new.email) = lower(admin_email);
  insert into public.profiles (id, full_name, email, role, status)
  values (
    new.id,
    coalesce(new.raw_user_meta_data->>'full_name', ''),
    coalesce(new.email, ''),
    case when is_owner then 'super_admin' else 'student' end,
    case when is_owner then 'approved' else 'pending' end
  );
  return new;
end $$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

create or replace function public.is_admin()
returns boolean language sql security definer stable set search_path = public as $$
  select exists (
    select 1 from public.profiles
    where id = auth.uid() and role = 'super_admin' and status = 'approved'
  );
$$;

drop policy if exists "read own profile" on public.profiles;
drop policy if exists "admin reads all" on public.profiles;
drop policy if exists "admin updates" on public.profiles;

create policy "read own profile" on public.profiles
  for select using (auth.uid() = id);
create policy "admin reads all" on public.profiles
  for select using (public.is_admin());
create policy "admin updates" on public.profiles
  for update using (public.is_admin()) with check (public.is_admin());
-- الطالب لا يملك أي سياسة تعديل: لا يستطيع تغيير حالته أو صلاحيته أبدًا.
