# ShopWise 🛍️✨ EL NUEVO NOMBRE DEL PROYECTO SERÁ CARRITO UNIVERSAL

**ShopWise** es una aplicación móvil Android nativa moderna, limpia, de alto rendimiento y visualmente inspirada en la interfaz **Samsung One UI**. Está diseñada para ayudarte a tomar decisiones inteligentes antes de comprar en línea analizando productos mediante Inteligencia Artificial (Google Gemini API), organizando tus deseos por prioridad en la base de datos Room y permitiéndote comparar ofertas y programar alarmas de recordatorio de forma totalmente sin esfuerzo.

---

## 🎨 Concepto y Diseño (Inspirado en Samsung One UI)
El diseño sigue de forma rigurosa los pilares del diseño de **One UI 6**:
*   **Encabezados Colosales**: Un gran espacio en la parte superior del Dashboard y pantallas de sección que facilitan el uso a una sola mano.
*   **Esquinas Redondeadas Suaves (Card-First)**: Formularios y cards de alta fidelidad con esquinas generosas de `20dp` a `24dp`.
*   **Minimalismo y Respiro**: Uso amplio de espacio negativo, tipografías elegantes, y un contraste bien cuidado tanto en temas claros como oscuros (con soporte AMOLED negro puro).

---

## 🚀 Arquitectura y Stack Tecnológico
La aplicación sigue la arquitectura oficial recomendada por Android (MVVM + Clean Repository):

### 1. El Framework Principal
- **Kotlin & Jetpack Compose**: Toda la interfaz de usuario está construida con código declarativo moderno y componentes Material Design 3.
- **Corrutinas y Flow**: Sincronización libre de bloqueos en hilos secundarios para la fluidez en el visor.

### 2. Capa de Datos Locales (Room Database + SharedPreferences)
- **Room SQLite**: Persistencia offline para el almacenamiento de deseos. Cuenta con una estructura de datos robusta para almacenar pros/contras, precios y fechas de alarmas.
- **Session Manager**: Registro seguro offline del estado del usuario mediante SharedPreferences.

### 3. Capa de Datos Remota (Retrofit + Gemini API)
- **Moshi y Retrofit**: Interacción directa con los servidores de Google mediante endpoints optimizados para invocar **Gemini 1.5 Flash**. Se configuró un tiempo de espera (`OkHttpClient` timeout) de 60 segundos por robustez.
- **Análisis de Enlaces Inteligente**: El algoritmo parsea de forma automatizada cualquier enlace de tienda (Amazon, Mercado Libre, AliExpress, Temu, Shopee), extrayendo títulos, logos, marcas, precios y ventajas sin infringir las políticas de uso de los sitios.

---

## 🔮 Funcionalidades Principales Desarrolladas

1.  **Registro e Inicios de Sesión Con Google (Simulado)**:
    Soporte multiusuario seguro con almacenamiento de token local. Incluye un chooser de cuentas predeterminadas y permite ingresar perfiles personalizados. Cumple de forma idónea con la protección legal al no exponer tokens de red del usuario.
2.  **Dashboard de Deseos de Compra**:
    Una vista global con estadísticas de presupuesto requerido, filtros táctiles por prioridad de compra (Alta, Media, Baja), filtros por estado de adquisición ("Por revisar", "Prioritorio", "En espera", "Comprado") y selectores de ordenamiento ágiles.
3.  **Análisis por Enlace en 3 Clics**:
    Consiste en copiar el link, pegarlo en la pantalla "Añadir deseo", y presionar "Análisis Inteligente". Gemini procesa la información y pre-llena un formulario con especificaciones estructuradas. También incluye adición manual como respaldo.
4.  **Ficha Técnica Individual y Markdown Exportador**:
    Cada producto tiene su propia hoja de vida Obsidian/Notion. Genera un reporte formateado en `.md` con tablas de ventajas y desventajas que el usuario puede copiar directamente al portapapeles o exportar usando los flujos de compartir nativos de Android mediante `FileProvider`.
5.  **Comparador de Categorías**:
    Permite contrastar cara a cara productos de una misma categoría. Un asistente inteligente sugiere automáticamente qué producto comprar primero según el algoritmo de satisfacción/costo.
6.  **Programación de Alarmas Locales (Snooze Inteligente)**:
    Configuración de recordatorios vía `AlarmManager` con notificaciones integradas al sistema. El usuario puede posponer decisiones (+1 hora, +24 horas) para evaluar fríamente si realizar la compra o ahorrar.

---

## 🗄️ Esquema del Modelo de Datos (ProductEntity)
La base de datos SQLite almacena un registro por cada deseo de compra con los siguientes campos:

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | `Int` | Identificador único autogenerado en SQLite (Clave Primaria). |
| `userId` | `String` | Correo electrónico del usuario asignado para aislamiento de cuenta. |
| `title` | `String` | Título técnico o nombre del artículo. |
| `url` | `String` | Enlace de compra original de la tienda. |
| `brand` | `String` | Marca comercial o fabricante del artículo. |
| `imageUrl` | `String` | Imagen de alta resolución del producto. |
| `price` | `Double` | Precio nominal de venta detectado. |
| `currency` | `String` | Símbolo de divisa ($ , €, COP, MXN). |
| `category` | `String` | Categoría de compras ("Tecnología", "Hogar", "Ropa", etc.). |
| `priority` | `String` | Prioridad del deseo ("Alta", "Media", "Baja"). |
| `status` | `String` | Estado ("Por revisar", "Prioritario", "En espera", "Comprado"). |
| `reminderDate`| `Long?` | Marca temporal UTC para el gatillo de notificaciones. |
| `rating` | `Float` | Valoración promedio en estrellas (0.0 - 5.0). |
| `summaryMd` | `String` | Reporte íntegro pre-generado por Gemini en formato Markdown. |
| `notes` | `String`| Comentarios personales del usuario. |
| `score` | `Int` | Puntuación de compra prioritaria unificada calculada de 1 a 100. |

---

## 🛠️ Credenciales y Configuración de Secretos
Para utilizar el motor de extracción inteligente, debes suministrar una clave Gemini API:
1.  Ingresa tu clave de API de Gemini dentro del panel de **Secrets** de Google AI Studio.
2.  La variable se mapea de forma completamente segura a `GEMINI_API_KEY` utilizando el plugin de Gradle de secretos.
3.  **Seguridad**: En caso de que no ingreses tu clave de API, la aplicación activará automáticamente una simulación offline limpia para que sigas operando sin bloqueos o caídas.

---

## 👨‍💻 Instrucciones de Expansión Futura
Si deseas escalar ShopWise, te sugerimos los siguientes caminos:
- **Campos de Base de Datos**: Si deseas rastrear cupones, añade `discountCode: String` o `targetPrice: Double` al archivo `ProductEntity.kt`. Luego, incrementa la versión de la base de datos en `AppDatabase.kt` a `2` e implementa una migración Room sencilla.
- **Mejorar Gemini**: Modifica el mensaje estructurado de prompting en `ProductRepository.kt` (líneas 63-95) para solicitar datos adicionales como dimensiones, compatibilidad ecológica, o alternativas más económicas recomendadas.
