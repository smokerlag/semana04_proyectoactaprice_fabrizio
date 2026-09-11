# Documento técnico — Acta PRICE

**Proyecto:** Aplicación móvil de fiscalización y generación de Acta PRICE (OSINERGMIN)  
**Stack app:** Android (Java), Room, Hilt, Retrofit, Aspose Words, iText  
**Stack API:** Node.js + Express + SQLite (`better-sqlite3`)

---

## 1. Arquitectura general

```
┌──────────────────────────┐         HTTPS/HTTP          ┌──────────────────────────┐
│   App Android (offline)  │  ◄──── sincronización ────► │   API REST (Express)     │
│                          │                             │                          │
│  UI Activities           │                             │  /auth/login             │
│  Room (SQLite local)     │                             │  /establecimientos       │
│  WordGenerator → PDF     │                             │  /fiscalizaciones        │
│  Retrofit ApiService     │                             │  SQLite acta_price.db    │
└──────────────────────────┘                             └──────────────────────────┘
```

### Principios
1. **Offline-first:** la fiscalización se completa sin red; Room guarda el acta.
2. **Sincronización diferida:** `ActaRepository` envía registros con `sincronizado = false`.
3. **Documento oficial:** se llena `plantilla.docx` y se exporta a PDF (Word → PDF).

### Capas de la app Android

| Capa | Paquete / archivos | Responsabilidad |
|------|--------------------|-----------------|
| Presentación | `*Activity`, layouts XML | Flujo de pantallas y captura de datos |
| Dominio / modelos | `model/` | Entidades Room y DTOs |
| Datos locales | `database/` (Room) | Persistencia SQLite en dispositivo |
| Red | `network/ApiService`, `di/NetworkModule` | Cliente Retrofit |
| Repositorio | `repository/ActaRepository` | CRUD + sync |
| Utilidades | `utils/WordGenerator`, `CsvUtil`, `SessionManager` | Acta PDF/Word, CSV, sesión |

---

## 2. Flujo funcional (app)

```
Login
 → Lista establecimientos (CSV precargado / alta manual)
  → Gestión establecimiento
   → 1. Datos generales
   → 2. Precios por combustible (PRICE / Publicado / Surtidor / Descuento + Marca)
   → 3. Verificación PRICE (Sí/No)
   → 4. Incumplimientos detectados
   → 5. Hechos verificados
   → 6. Firmas / receptor / documentación
   → 7. Preview → guardar Room + generar DOCX/PDF
```

Los datos viajan entre Activities por **Intent extras** y se consolidan en `FiscalizacionPreviewActivity`.

---

## 3. Endpoints de la API REST

**Base URL local:** `http://localhost:3000`  
**Emulador Android → PC:** `http://10.0.2.2:3000/`

### 3.1 `POST /auth/login`
Autenticación de fiscalizador.

**Request**
```json
{ "username": "fiscalizador", "password": "price2026" }
```

**Response 200**
```json
{ "token": "jwt-...", "error": null, "nombre": "...", "rol": "FISCALIZADOR" }
```

**Response 401**
```json
{ "token": null, "error": "Credenciales inválidas" }
```

### 3.2 `GET /establecimientos`
Lista todos los establecimientos.

**Response 200:** `Establecimiento[]`

### 3.3 `POST /establecimientos`
Crea un establecimiento (sync desde la app).

**Request:** cuerpo `Establecimiento`  
**Response 201:** establecimiento creado (`sincronizado = true` en servidor)

### 3.4 `GET /fiscalizaciones`
Lista fiscalizaciones registradas.

**Response 200:** `Fiscalizacion[]`

### 3.5 `POST /fiscalizaciones`
Registra una fiscalización completa (incluye JSON de productos/hechos).

**Request:** cuerpo `Fiscalizacion` (requiere `establecimientoId`)  
**Response 201:** fiscalización creada

Estos contratos coinciden con `com.example.actapriceproyect.network.ApiService`.

---

## 4. Base de datos

### 4.1 App (Room)
- Nombre: `acta_price_db`
- Versión: 5
- Migración: `fallbackToDestructiveMigration()`
- Script equivalente: `entrega/database/acta_price_schema.sql`

### 4.2 Tablas principales

#### `establecimientos`
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INTEGER PK AI | |
| nombre, ruc, direccion, telefono, ubigeo | TEXT | |
| actividad, nroRegistro, fechaEmision, placaPrincipal | TEXT | |
| sincronizado | INTEGER/boolean | flag de sync |

#### `fiscalizaciones`
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INTEGER PK AI | |
| establecimientoId | INTEGER FK | |
| expediente … fiscalizadorResponsable | TEXT | datos generales |
| productosJson | TEXT | array productos/precios |
| telefonoPublicado … etiquetaVisible | TEXT | verificación q1–q6 |
| incumplimientosJson, hechosVerificados | TEXT | JSON |
| documentacion, ocurrencias, observaciones | TEXT | |
| negativaFirma | INTEGER/boolean | |
| firmaInspectorPath, firmaResponsablePath | TEXT | rutas locales |
| sincronizado, estado | | BORRADOR / ACTA GENERADA |
| latitud, longitud, fotosJson, historialCambios | | avanzados |

#### `usuarios` (solo API)
Credenciales de acceso al backend.

### 4.3 Relación
```
establecimientos 1 ─── N fiscalizaciones
```

---

## 5. Generación del Acta PRICE

1. Plantilla: `app/src/main/assets/plantilla.docx`
2. `WordGenerator` reemplaza placeholders (`{{agente}}`, `{{q1_si}}`, etc.)
3. Llena tablas de precios y hechos
4. Inserta firmas táctiles
5. Exporta `.docx` y `.pdf` a almacenamiento de la app (`Documents/`)
6. Si el PDF queda en 1 página, se fuerza página 2 (III. OTROS + firmas) mediante split+merge

---

## 6. Seguridad y limitaciones actuales

- Login en app puede operar en modo local (demo); la API ofrece login real.
- Tokens JWT de demo (UUID); no hay refresh ni roles en middleware.
- Firmas/fotos se guardan como rutas locales del dispositivo (no se suben binarios a la API en esta versión).
- URL base configurable en `NetworkModule.java`.

---

## 7. Cómo ejecutar el ecosistema

### API
```bash
cd entrega/api
npm install
npm start
```

### App
1. Abrir el proyecto en Android Studio
2. Configurar `BASE_URL` en `NetworkModule` (`http://10.0.2.2:3000/` para emulador)
3. Run → `app`

### Pruebas API
Importar `entrega/postman/Acta_PRICE_API.postman_collection.json` en Postman y ejecutar la colección.

---

## 8. Entregables relacionados

| Entregable | Ubicación |
|------------|-----------|
| Proyecto Android | raíz del repositorio |
| Código API | `entrega/api/` |
| Script SQLite | `entrega/database/acta_price_schema.sql` |
| Postman | `entrega/postman/` |
| Evidencias (capturas/video/PDF) | `entrega/evidencias/` + guía |
| Este documento | `entrega/docs/Documento_Tecnico_ActaPRICE.md` |
