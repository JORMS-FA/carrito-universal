# Carrito Universal

Carrito Universal es una app Android para guardar productos, analizarlos con IA y comparar deseos de compra antes de decidir.

## Funciones

- Guardar productos desde enlaces compartidos del navegador.
- Analizar enlaces con Gemini usando la clave API personal del usuario.
- Iniciar sesion con correo mediante Supabase Auth o continuar como invitado local.
- Organizar productos por categoria, prioridad, estado y recordatorios.
- Comparar productos y exportar fichas en Markdown.
- Generar APK local optimizado sin Android Studio ni emulador.

## Configuracion

Crea un archivo `.env` en la raiz:

```env
GEMINI_API_KEY=USER_CONFIGURED_IN_APP
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-publishable-key
```

La clave Gemini no se comparte globalmente: cada usuario la agrega dentro de la app en `Ajustes > Clave Gemini API`.

## Supabase

1. Activa Email/Password en Supabase Auth.
2. Ejecuta `supabase/migrations/202605240001_create_products.sql`.
3. Usa solo la publishable/anon key en Android. No uses `service_role`.

## APK Local

```powershell
powershell -ExecutionPolicy Bypass -File .\build-local.ps1
```

El APK queda en:

```text
dist-local/carrito-universal-opt.apk
```

## Notas

El modo invitado guarda datos solo en el dispositivo. Las cuentas Supabase quedan listas para sincronizacion con una futura version web.
