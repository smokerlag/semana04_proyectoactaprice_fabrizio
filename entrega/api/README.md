# API REST — Acta PRICE

API de sincronización alineada con `ApiService` de la app Android.

## Requisitos
- Node.js 18+ (sin compiladores nativos; usa `sql.js`)

## Instalación y arranque

```bash
cd entrega/api
npm install
npm start
```

Queda en: `http://localhost:3000`

La base SQLite se crea automáticamente en `entrega/api/data/acta_price.db` usando el script `entrega/database/acta_price_schema.sql`.

## Auth
1. `POST /auth/login` → obtiene `token`
2. Rutas protegidas requieren header:
   ```http
   Authorization: Bearer <token>
   ```
3. Sin token → `401`

En Postman: ejecuta primero **Auth - Login OK** (guarda `{{token}}`). Luego los demás requests ya llevan el Bearer.

## Endpoints
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/auth/login` | Login |
| GET | `/establecimientos` | Listar |
| POST | `/establecimientos` | Crear |
| GET | `/fiscalizaciones` | Listar |
| POST | `/fiscalizaciones` | Registrar |

## Android emulator
En `NetworkModule.java` usar:

```text
http://10.0.2.2:3000/
```

(`10.0.2.2` = localhost del PC desde el emulador)
