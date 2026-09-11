# Acta PRICE — Documentación del proyecto

**Asignatura / entrega:** Semana 04  
**Objetivo:** Aplicación móvil Android para el levantamiento de datos de fiscalización en establecimientos de hidrocarburos y la generación del **Acta PRICE** (formato oficial DOCX/PDF), con backend REST de autenticación y sincronización.

---

## 1. Resumen técnico

| Elemento | Detalle |
|----------|---------|
| Cliente | Android nativo (Java), `minSdk 26`, `targetSdk 35` |
| Arquitectura app | Activities + Room (SQLite) + Hilt (DI) + Retrofit |
| Generación documental | Apache POI (llenado de `plantilla.docx`) + conversión PDF vía API |
| Backend | Node.js 18+, Express 4, SQLite embebido con `sql.js` |
| Auth | Token Bearer emitido en login (sesión en memoria en API) |
| Pruebas API | Colección Postman incluida |
| Plantilla | `app/src/main/assets/plantilla.docx` (única plantilla versionada) |

---

## 2. Arquitectura

```
┌─────────────────────────────────────┐         HTTP (LAN / emulador)        ┌─────────────────────────────────────┐
│  App Android                        │ ◄──────────────────────────────────► │  API REST (Express)                  │
│                                     │   /auth/login                        │                                     │
│  LoginActivity                      │   /establecimientos                  │  Auth Bearer                        │
│  Flujo fiscalización (wizard)       │   /fiscalizaciones                   │  Persistencia SQLite (sql.js)        │
│  Room (offline-first)               │   /documentos/docx-to-pdf            │  Word COM → PDF (Windows)            │
│  WordGenerator (POI)                │                                      │  scripts/docx2pdf.ps1                │
│  SessionManager + OkHttp interceptor│                                      │                                     │
└─────────────────────────────────────┘                                      └─────────────────────────────────────┘
```

### Decisiones de diseño relevantes

1. **Offline-first:** la fiscalización se completa en el dispositivo; Room persiste el acta aunque no haya red.
2. **Login con fallback:** si la API no responde, la app entra en modo offline (token demo) para no bloquear la demo; el PDF fiel requiere API + Word.
3. **Documento oficial:** no se redibuja el acta con un PDF “genérico”; se reutiliza la plantilla Word institucional.
4. **PDF de 2 páginas:** la conversión se delega a Microsoft Word en el PC (Automation/COM) porque librerías móviles de evaluación (p. ej. Aspose) truncaban el documento.
5. **Hechos verificados condicionales:** la pantalla de hechos solo aparece si existen incumplimientos 1–6; cada texto se inserta en la fila correspondiente de la tabla del acta.

---

## 3. Flujo funcional (app)

```
Login
 → Establecimientos (CSV local / alta / Room)
  → Datos generales (validaciones de expediente, registro, teléfono, etc.)
  → Precios por producto (PRICE / Publicado / Surtidor / Descuento; Otros + marca)
  → Verificación Sí/No (teléfono, horario, galón, etiqueta, lista)
  → Incumplimientos detectados automáticamente
  → [Opcional] Hechos verificados (solo si hay incumplimientos 1–6)
  → Firmas (fiscalizador obligatorio; receptor o negativa)
  → Preview → generación DOCX + PDF
```

### Validaciones de entrada (extracto)

| Campo | Regla |
|-------|--------|
| Número de expediente | Solo dígitos |
| Registro de hidrocarburos | Solo dígitos |
| Teléfono/Fax | Exactamente 9 dígitos |
| DNI receptor | Exactamente 8 dígitos |

---

## 4. Generación del Acta (DOCX / PDF)

| Paso | Componente | Descripción |
|------|------------|-------------|
| 1 | `WordGenerator` | Carga `plantilla.docx`, reemplaza placeholders, llena tablas de precios y hechos (POI) |
| 2 | Firmas | Imágenes PNG en celdas: col. 0 fiscalizador, col. 1 receptor |
| 3 | Persistencia local | Guarda `Acta_PRICE_<timestamp>.docx` en almacenamiento de la app |
| 4 | API | `POST /documentos/docx-to-pdf` con `docxBase64` |
| 5 | Word (PC) | `entrega/api/scripts/docx2pdf.ps1` exporta PDF (`wdFormatPDF`) |
| 6 | App | Recibe `pdfBase64`, guarda PDF y abre/comparte |

**Dependencia crítica del PDF:** sistema operativo Windows con **Microsoft Word** instalado en la máquina que ejecuta la API.

---

## 5. API REST

### Arranque

```bash
cd entrega/api
npm install
npm start
```

- Puerto por defecto: `3000` (`PORT` por env).
- Bind: `0.0.0.0` (accesible en LAN).
- BD: `entrega/api/data/acta_price.db` (se crea/inicializa desde `entrega/database/acta_price_schema.sql`).

### Credenciales de demostración

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| `fiscalizador` | `price2026` | FISCALIZADOR |
| `admin` | `admin123` | ADMIN |

### Endpoints

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| POST | `/auth/login` | No | Emite token Bearer |
| GET/POST | `/establecimientos` | Bearer | CRUD listado/alta |
| GET/POST | `/fiscalizaciones` | Bearer | Listado/registro |
| POST | `/documentos/docx-to-pdf` | No* | Convierte DOCX→PDF vía Word |

\*El endpoint de conversión está pensado para el cliente de la app en entorno de laboratorio; en producción debería protegerse igual que el resto.

### Contrato PDF (resumen)

**Request JSON**
```json
{ "docxBase64": "<base64>", "fileName": "Acta_PRICE.docx" }
```

**Response JSON**
```json
{ "pdfBase64": "<base64>", "error": null }
```

Colección Postman: `entrega/postman/Acta_PRICE_API.postman_collection.json`.

---

## 6. Configuración de red (app ↔ API)

Archivo: `app/src/main/java/.../di/NetworkModule.java`

| Escenario | `BASE_URL` |
|-----------|------------|
| Emulador Android | `http://10.0.2.2:3000/` |
| Dispositivo físico (misma Wi‑Fi) | `http://<IPv4-del-PC>:3000/` |

Obtener IPv4 en Windows: `ipconfig`.  
La app permite cleartext HTTP (`usesCleartextTraffic`) para entorno académico local.

---

## 7. Estructura del repositorio

```
semana04_proyectoactaprice_fabrizio/
├── app/                          # Módulo Android
│   └── src/main/
│       ├── java/.../             # Activities, DI, Room, red, WordGenerator
│       ├── res/                  # Layouts y recursos
│       └── assets/
│           ├── plantilla.docx    # Plantilla oficial del acta
│           └── establecimientos.csv
├── entrega/
│   ├── api/                      # Backend Express + sql.js + script Word
│   ├── database/                 # Script SQL de esquema
│   └── postman/                  # Colección de pruebas
├── gradle/                       # Catálogo de dependencias
└── README.md                     # Documentación técnica del proyecto
```

---

## 8. Stack y dependencias principales

### Android
- AndroidX AppCompat / Material
- Room + Hilt + Retrofit/Gson
- Apache POI (`poi-ooxml`) — manipulación DOCX
- iText 7 — conteo/utilidades PDF en cliente

### API
- `express`, `cors`, `uuid`
- `sql.js` (SQLite en WASM/JS, sin toolchain nativo)
- PowerShell + Word COM para PDF

---

## 9. Procedimiento de evaluación / reproducción

1. Instalar **Node.js 18+** y **Microsoft Word** (Windows).
2. Ejecutar `npm install` y `npm start` en `entrega/api`.
3. Abrir el proyecto raíz en **Android Studio**, ajustar `BASE_URL` según emulador/dispositivo.
4. Compilar e instalar la app; iniciar sesión con `fiscalizador` / `price2026`.
5. Completar una fiscalización de extremo a extremo y verificar DOCX + PDF (2 páginas).
6. (Opcional) Importar la colección Postman y validar login + endpoints protegidos.

### Criterios de verificación sugeridos

- [ ] Login online obtiene token y no cae a offline sin causa.
- [ ] Validaciones de expediente / teléfono / DNI bloquean datos inválidos.
- [ ] Hechos solo se solicitan si hay incumplimientos 1–6.
- [ ] Firmas: fiscalizador a la izquierda, receptor a la derecha.
- [ ] PDF resultante conserva estructura de 2 páginas de la plantilla.

---

## 10. Limitaciones conocidas (ámbito académico)

- La conversión PDF depende de Word en el host de la API (no es multiplataforma).
- Los tokens de API son opacos en memoria (no JWT firmado ni Redis).
- El modo offline permite continuar la demo sin red, pero no sustituye la conversión PDF remota.
