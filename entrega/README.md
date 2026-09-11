# Entrega — Proyecto Acta PRICE

Paquete de evidencias y componentes del proyecto de fiscalización **Acta PRICE**.

## Contenido

| # | Entregable | Ubicación | Estado |
|---|------------|-----------|--------|
| 1 | Proyecto Android Studio completo | Raíz del repositorio (`app/`, `gradle/`, etc.) | Listo |
| 2 | Código fuente API REST | `entrega/api/` | Listo |
| 3 | Script SQLite | `entrega/database/acta_price_schema.sql` | Listo |
| 4 | Colección Postman | `entrega/postman/Acta_PRICE_API.postman_collection.json` | Listo |
| 5 | Capturas de pantalla | `entrega/evidencias/capturas/` | **Pendiente (tú)** — ver guía |
| 6 | Video corto del flujo | `entrega/evidencias/video/` | **Pendiente (tú)** — ver guía |
| 7 | PDF final Acta PRICE | `entrega/evidencias/pdf/` | **Pendiente (tú)** — ver guía |
| 8 | Documento técnico | `entrega/docs/Documento_Tecnico_ActaPRICE.md` | Listo |

Guía paso a paso para 5–7: [`evidencias/GUIA_EVIDENCIAS.md`](evidencias/GUIA_EVIDENCIAS.md)

---

## Arranque rápido

### 1) API
```bash
cd entrega/api
npm install
npm start
```
→ `http://localhost:3000`

Usuarios: `admin/admin123` · `fiscalizador/price2026`

### 2) Postman
1. Importar `entrega/postman/Acta_PRICE_API.postman_collection.json`
2. Ejecutar la colección (API debe estar arriba)

### 3) App Android
1. Abrir la carpeta raíz en Android Studio
2. En `NetworkModule.java`, base URL para emulador:
   `http://10.0.2.2:3000/`
3. Run `app`

### 4) Evidencias visuales
Sigue `entrega/evidencias/GUIA_EVIDENCIAS.md` y guarda capturas/video/PDF en las carpetas indicadas.

---

## Estructura

```
entrega/
├── README.md                          ← este archivo
├── api/                               ← API REST Node/Express + SQLite
├── database/acta_price_schema.sql     ← script SQLite
├── postman/                           ← colección de pruebas
├── docs/Documento_Tecnico_ActaPRICE.md
└── evidencias/
    ├── GUIA_EVIDENCIAS.md
    ├── capturas/
    ├── video/
    └── pdf/
```
