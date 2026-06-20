# FRD — Functional Requirements Document

## Carrito Universal Platform

---

## 1. Visión General

Carrito Universal es una plataforma multiplataforma para guardar productos, analizarlos con inteligencia artificial (Gemini), comparar deseos de compra y tomar decisiones informadas. Está compuesta por dos aplicaciones frontend que comparten un mismo backend en Supabase:

| Componente | Tecnología | Propósito |
|---|---|---|
| **carrito-universal** (Android) | Kotlin + Jetpack Compose | App móvil nativa. Wishlist personal con análisis IA, comparador, alertas de precio, recordatorios, exportación Markdown. |
| **carrito-universal-web** | Astro 5 + React 18 + Tailwind | App web companion. Wishlist + carrito de compras con persistencia local y checkout futuro. |
| **Supabase** | PostgreSQL + Auth + RLS | Backend único compartido. Autenticación (email/password, Google OAuth), base de datos de productos, políticas de seguridad por fila. |

---

## 2. Roles de Usuario

### 2.1 Invitado (sin sesión)
- Acceso solo en Android.
- Los productos se guardan en Room (base de datos local SQLite).
- No hay persistencia en la nube.
- Al cerrar sesión o borrar datos de la app, se pierde toda la información.
- Puede convertirse a usuario registrado sin perder datos locales.

### 2.2 Registrado (con sesión Supabase)
- Disponible en Android y Web.
- Productos sincronizados con Supabase (tabla `products` con RLS por `user_id`).
- Puede acceder desde cualquier dispositivo.
- Funcionalidades completas: análisis IA, comparador, alertas, recordatorios, carrito web.

---

## 3. Módulos Funcionales

### 3.1 Autenticación

| # | Requisito | Android | Web |
|---|---|---|---|
| FR-AUTH-01 | Iniciar sesión con email y contraseña | Sí (LoginScreen) | Sí (AuthModal) |
| FR-AUTH-02 | Registrarse con email y contraseña | Sí | Sí |
| FR-AUTH-03 | Iniciar sesión con Google OAuth | Sí (Credential Manager + GoogleIdToken) | Sí (Supabase OAuth redirect) |
| FR-AUTH-04 | Cerrar sesión | Sí | Sí |
| FR-AUTH-05 | Recuperar sesión al reabrir la app | Sí (SessionManager con EncryptedSharedPreferences) | Sí (Supabase onAuthStateChange) |
| FR-AUTH-06 | Modo invitado | Sí (datos locales en Room) | No (requiere sesión) |

### 3.2 Gestión de Productos/Deseos

| # | Requisito | Android | Web |
|---|---|---|---|
| FR-PROD-01 | Crear producto pegando un enlace (URL) | Sí | — |
| FR-PROD-02 | Analizar enlace con Gemini AI (extraer título, precio, marca, tienda, pros/contras, score, etc.) | Sí | — |
| FR-PROD-03 | Crear producto manualmente (rellenar formulario) | Sí | Pendiente |
| FR-PROD-04 | Editar todos los campos de un producto | Sí (ProductDetailScreen) | Pendiente |
| FR-PROD-05 | Eliminar un producto | Sí | Pendiente |
| FR-PROD-06 | Listar productos del usuario actual | Sí | Sí (desde Supabase) |
| FR-PROD-07 | Ver detalle completo de un producto | Sí (ProductDetailScreen) | Pendiente |

**Campos del producto** (modelo unificado, 27 campos):

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | INTEGER (PK, auto-generado) | Identificador único |
| `userId` | TEXT | UUID del usuario en Supabase (vacío para invitados) |
| `title` | TEXT | Nombre del producto |
| `url` | TEXT | Enlace original del producto |
| `brand` | TEXT | Marca |
| `imageUrl` | TEXT | URL de la imagen principal |
| `price` | REAL | Precio numérico |
| `currency` | TEXT | Símbolo de moneda (ej. $, €) |
| `category` | TEXT | Categoría (Tecnología, Hogar, Ropa, etc.) |
| `priority` | TEXT | Prioridad: Baja / Media / Alta |
| `status` | TEXT | Estado: Por revisar / Prioritario / En espera / Comprado / Descartado |
| `reminderDate` | TEXT (nullable) | Fecha ISO para recordatorio |
| `rating` | REAL | Valoración 0.0–5.0 |
| `summaryMd` | TEXT | Resumen en Markdown generado por Gemini |
| `notes` | TEXT | Notas personales del usuario |
| `sourceStore` | TEXT | Tienda/origen (Amazon, Mercado Libre, etc.) |
| `comparisonGroupId` | TEXT (nullable) | ID de grupo para comparaciones |
| `quality` | INTEGER | Calidad percibida 1–5 estrellas |
| `pros` | TEXT | Ventajas (separadas por comas) |
| `contras` | TEXT | Desventajas (separadas por comas) |
| `score` | INTEGER | Puntuación IA 0–100 |
| `priceAlertEnabled` | INTEGER (0/1) | Alerta de precio activada |
| `priceAlertThreshold` | REAL | Umbral para notificar |
| `priceHistory` | TEXT | Historial de precios (timestamp:precio\|timestamp:precio) |
| `createdAt` | INTEGER | Timestamp de creación (epoch millis) |
| `updatedAt` | INTEGER | Timestamp de última modificación (epoch millis) |
| `createdAtISO` | TEXT (nullable) | Fecha ISO de creación |
| `updatedAtISO` | TEXT (nullable) | Fecha ISO de última modificación |

### 3.3 Dashboard y Organización

| # | Requisito | Android | Web |
|---|---|---|---|
| FR-DASH-01 | Ver tarjetas de resumen: presupuesto total pendiente, productos urgentes, total guardados | Sí | — |
| FR-DASH-02 | Ver avatar y nombre de usuario | Sí | En Header |
| FR-DASH-03 | Filtrar productos por estado (Todos / Por revisar / Prioritario / En espera / Comprado / Descartado) | Sí | Pendiente |
| FR-DASH-04 | Filtrar productos por prioridad (Todas / Alta / Media / Baja) | Sí | Pendiente |
| FR-DASH-05 | Ordenar productos por (Fecha reciente / Precio mayor / Precio menor / Puntuación IA) | Sí | Pendiente |
| FR-DASH-06 | Ver lista/scroll de productos con tarjetas visuales | Sí | Sí (grid en index.astro) |
| FR-DASH-07 | Navegar a detalle del producto desde la tarjeta | Sí | Pendiente |

### 3.4 Comparador Inteligente

| # | Requisito | Android | Web |
|---|---|---|---|
| FR-COMP-01 | Seleccionar categoría para agrupar productos comparables | Sí | Pendiente |
| FR-COMP-02 | Ver recomendación del mejor producto según score IA | Sí | Pendiente |
| FR-COMP-03 | Ver tarjetas comparativas lado a lado con precio, calidad, pros clave, contras clave | Sí | Pendiente |
| FR-COMP-04 | Navegar al detalle del producto desde la comparativa | Sí | Pendiente |
| FR-COMP-05 | Mostrar mensaje si hay menos de 2 productos en la categoría | Sí | Pendiente |
| FR-COMP-06 | Mostrar mensaje si no hay productos en ninguna categoría | Sí | Pendiente |

### 3.5 Grill Me (Asistente Interactivo de Decisión)

**Propuesta de nueva funcionalidad para ambas apps.**

| # | Requisito | Android | Web |
|---|---|---|---|
| FR-GRILL-01 | Seleccionar 2 o más productos de una misma categoría | Pendiente | Pendiente |
| FR-GRILL-02 | Enviar los productos a Gemini para un análisis comparativo profundo | Pendiente | Pendiente |
| FR-GRILL-03 | Mostrar tabla comparativa generada por IA: especificaciones, precio, calidad, pros/contras enfrentados | Pendiente | Pendiente |
| FR-GRILL-04 | Mostrar veredicto final con recomendación y justificación | Pendiente | Pendiente |
| FR-GRILL-05 | Mostrar métrica de relación calidad-precio calculada por IA | Pendiente | Pendiente |
| FR-GRILL-06 | Permitir guardar el resultado del grill como nota en Markdown | Pendiente | Pendiente |

### 3.6 Alertas de Precio

| # | Requisito | Android | Web |
|---|---|---|---|
| FR-ALERT-01 | Activar/desactivar alerta de precio por producto | Sí | Pendiente |
| FR-ALERT-02 | Configurar umbral de precio para notificar | Sí | Pendiente |
| FR-ALERT-03 | Enviar notificación push cuando el precio baje del umbral | Sí (AlarmReceiver + NotificationHelper) | Pendiente (requiere push) |

### 3.7 Recordatorios

| # | Requisito | Android | Web |
|---|---|---|---|
| FR-REM-01 | Asignar fecha de recordatorio a un producto | Sí (ReminderScreen) | Pendiente |
| FR-REM-02 | Recibir notificación en la fecha programada | Sí (AlarmManager + NotificationHelper) | Pendiente (requiere push) |
| FR-REM-03 | Posponer o marcar como completado el recordatorio | Sí | Pendiente |

### 3.8 Carrito de Compras (Web)

| # | Requisito | Android | Web |
|---|---|---|---|
| FR-CART-01 | Añadir producto al carrito desde la tarjeta | — | Sí |
| FR-CART-02 | Ver carrito en panel lateral deslizante (drawer) | — | Sí |
| FR-CART-03 | Ajustar cantidad por producto (+/−) | — | Sí |
| FR-CART-04 | Eliminar producto del carrito | — | Sí |
| FR-CART-05 | Ver total acumulado del carrito | — | Sí |
| FR-CART-06 | Vaciar carrito completo | — | Sí |
| FR-CART-07 | Persistir carrito entre sesiones (localStorage) | — | Sí |
| FR-CART-08 | Botón "Pagar ahora" (placeholder para checkout futuro) | — | Sí |

### 3.9 Exportación

| # | Requisito | Android | Web |
|---|---|---|---|
| FR-EXPORT-01 | Exportar lista de productos a Markdown | Sí (MarkdownExporter + MarkdownDocumentScreen) | Pendiente |
| FR-EXPORT-02 | Compartir/guardar el documento Markdown generado | Sí | Pendiente |

### 3.10 Perfil y Configuración

| # | Requisito | Android | Web |
|---|---|---|---|
| FR-SETT-01 | Ver/editar nombre de usuario | Sí (ProfileSettingsScreen) | — |
| FR-SETT-02 | Cambiar contraseña | Sí | — |
| FR-SETT-03 | Configurar Gemini API Key | Sí (guardada en EncryptedSharedPreferences) | — |
| FR-SETT-04 | Cerrar sesión | Sí | Sí (Header) |
| FR-SETT-05 | Eliminar cuenta | Sí | — |
| FR-SETT-06 | Cambiar tema claro/oscuro | Sí (Theme.kt) | Sí (ThemeToggle + localStorage) |

### 3.11 Onboarding

| # | Requisito | Android | Web |
|---|---|---|---|
| FR-ONBRD-01 | Mostrar pantallas de bienvenida al primer inicio | Sí (OnboardingScreen con 3 pasos) | — |
| FR-ONBRD-02 | Permitir iniciar como invitado desde onboarding | Sí | — |
| FR-ONBRD-03 | Permitir registrarse/iniciar sesión desde onboarding | Sí | — |

---

## 4. Restricciones Técnicas

| ID | Restricción |
|---|---|
| RT-01 | Un solo backend Supabase compartido para ambas apps. |
| RT-02 | Android: offline-first. Room como fuente de verdad local, Supabase para persistencia en la nube y sync. |
| RT-03 | Web: online-first. Lectura directa de Supabase. Sin base de datos local. |
| RT-04 | La tabla `products` en Supabase debe usar el modelo completo de 27 campos del Android ProductEntity. |
| RT-05 | Políticas RLS en Supabase: cada usuario solo ve y modifica sus propios productos (`user_id = auth.uid()`). |
| RT-06 | Gemini API Key es configurada por cada usuario dentro de la app Android (no compartida globalmente). |
| RT-07 | La web no tiene acceso a Gemini (no hay clave del lado cliente). El análisis IA solo ocurre en Android. |
| RT-08 | Carrito web: solo persistencia local (Zustand + localStorage). Sin tabla de carrito en Supabase. |

---

## 5. Glosario

| Término | Definición |
|---|---|
| **Producto / Deseo** | Cualquier artículo que el usuario guarda para considerar su compra. |
| **Score** | Puntuación 0–100 asignada por Gemini según calidad, precio, pros/contras. |
| **Grill Me** | Análisis comparativo profundo entre 2+ productos usando IA. |
| **RLS** | Row Level Security. Políticas de Supabase que restringen acceso a filas por usuario. |
| **Wishlist** | Lista de deseos. El conjunto de productos guardados por el usuario. |
