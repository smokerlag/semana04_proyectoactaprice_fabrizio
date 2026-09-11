-- =============================================================================
-- Acta PRICE - Script SQLite
-- Compatible con el esquema Room (AppDatabase v5) de la app Android
-- Base de datos: acta_price_db
-- =============================================================================

PRAGMA foreign_keys = ON;

-- -----------------------------------------------------------------------------
-- Tabla: establecimientos
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS establecimientos (
    id              INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    nombre          TEXT,
    ruc             TEXT,
    direccion       TEXT,
    telefono        TEXT,          -- en la app se usa también como Código Osinergmin
    ubigeo          TEXT,
    actividad       TEXT,
    nroRegistro     TEXT,
    fechaEmision    TEXT,
    placaPrincipal  TEXT,
    sincronizado    INTEGER NOT NULL DEFAULT 0  -- 0=false, 1=true
);

CREATE INDEX IF NOT EXISTS idx_establecimientos_ruc ON establecimientos(ruc);
CREATE INDEX IF NOT EXISTS idx_establecimientos_sync ON establecimientos(sincronizado);

-- -----------------------------------------------------------------------------
-- Tabla: fiscalizaciones
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS fiscalizaciones (
    id                          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    establecimientoId           INTEGER NOT NULL,
    -- Datos generales
    expediente                  TEXT,
    agenteFiscalizado           TEXT,
    codigoOsinergmin            TEXT,
    registroHidrocarburos       TEXT,
    fechaDiligencia             TEXT,
    horaApertura                TEXT,
    horaCierre                  TEXT,
    direccion                   TEXT,
    distrito                    TEXT,
    provincia                   TEXT,
    departamento                TEXT,
    rucDni                      TEXT,
    telefonoFax                 TEXT,
    fiscalizadorResponsable     TEXT,
    -- Información recabada (JSON)
    productosJson               TEXT,
    -- Verificación PRICE
    telefonoPublicado           TEXT,
    telefonoActualizado         TEXT,
    horarioPublicado            TEXT,
    listaPreciosExhibida        TEXT,
    unidadGalonEmpleada         TEXT,
    etiquetaVisible             TEXT,
    -- Incumplimientos / hechos (JSON)
    incumplimientosJson         TEXT,
    hechosVerificados           TEXT,
    -- Otros y firmas
    documentacion               TEXT,
    ocurrencias                 TEXT,
    observaciones               TEXT,
    negativaFirma               INTEGER NOT NULL DEFAULT 0,
    firmaInspectorPath          TEXT,
    firmaResponsablePath        TEXT,
    -- Meta / sync
    sincronizado                INTEGER NOT NULL DEFAULT 0,
    estado                      TEXT DEFAULT 'BORRADOR',
    latitud                     REAL NOT NULL DEFAULT 0,
    longitud                    REAL NOT NULL DEFAULT 0,
    fotosJson                   TEXT,
    historialCambios            TEXT,
    FOREIGN KEY (establecimientoId) REFERENCES establecimientos(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_fiscalizaciones_est ON fiscalizaciones(establecimientoId);
CREATE INDEX IF NOT EXISTS idx_fiscalizaciones_sync ON fiscalizaciones(sincronizado);
CREATE INDEX IF NOT EXISTS idx_fiscalizaciones_estado ON fiscalizaciones(estado);

-- -----------------------------------------------------------------------------
-- Tabla auxiliar API: usuarios (solo backend / autenticación)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    username    TEXT NOT NULL UNIQUE,
    password    TEXT NOT NULL,
    nombre      TEXT,
    rol         TEXT DEFAULT 'FISCALIZADOR'
);

-- -----------------------------------------------------------------------------
-- Datos de ejemplo
-- -----------------------------------------------------------------------------
INSERT OR IGNORE INTO usuarios (id, username, password, nombre, rol) VALUES
 (1, 'admin', 'admin123', 'Administrador', 'ADMIN'),
 (2, 'fiscalizador', 'price2026', 'Fiscalizador OSINERGMIN', 'FISCALIZADOR');

INSERT OR IGNORE INTO establecimientos (
    id, nombre, ruc, direccion, telefono, ubigeo, actividad, nroRegistro, fechaEmision, placaPrincipal, sincronizado
) VALUES (
    1,
    'ESTACION DE SERVICIO DEMO',
    '20123456789',
    'AV. EJEMPLO 123',
    '150123',
    'LIMA-LIMA-MIRAFLORES',
    'GRIFO',
    'RH-0001',
    '01/01/2024',
    '',
    0
);

-- Consultas de verificación
-- SELECT * FROM establecimientos;
-- SELECT * FROM fiscalizaciones;
-- SELECT * FROM usuarios;
