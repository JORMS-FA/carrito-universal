# Supabase setup

Este proyecto usa Supabase para cuentas sincronizadas y deja el modo invitado como almacenamiento local sin nube.

## Variables Android

Agrega estas claves en `.env`:

```env
SUPABASE_URL=https://your-project-ref.supabase.co
SUPABASE_ANON_KEY=your-supabase-publishable-or-anon-key
```

La `SUPABASE_ANON_KEY` puede ser la publishable key nueva o la anon key legacy. No uses la `service_role` en Android.

## Auth

Activa Email/Password en Supabase Auth. Para pruebas rapidas, puedes desactivar la confirmacion por email; para produccion, deja la confirmacion activa y agrega el flujo de verificacion.

## Base de datos

Ejecuta el SQL de `migrations/202605240001_create_products.sql` en el SQL Editor de Supabase. La tabla `products` usa `user_id = auth.uid()` para que Android y la futura version web compartan datos con las mismas politicas RLS.

## Pendiente

La app ya tiene login, registro y modo invitado. La siguiente fase es sincronizar la tabla local Room `products` con `public.products` en Supabase.
