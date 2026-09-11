const fs = require('fs');
const path = require('path');
const express = require('express');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');
const initSqlJs = require('sql.js');

const PORT = process.env.PORT || 3000;
const dataDir = path.join(__dirname, '..', 'data');
const dbPath = path.join(dataDir, 'acta_price.db');
const schemaPath = path.join(__dirname, '..', '..', 'database', 'acta_price_schema.sql');
const scriptsDir = path.join(__dirname, '..', 'scripts');
const tmpDir = path.join(dataDir, 'tmp');

if (!fs.existsSync(dataDir)) fs.mkdirSync(dataDir, { recursive: true });
if (!fs.existsSync(tmpDir)) fs.mkdirSync(tmpDir, { recursive: true });

const { execFile } = require('child_process');
const { promisify } = require('util');
const execFileAsync = promisify(execFile);

let db;

function persist() {
  const data = db.export();
  fs.writeFileSync(dbPath, Buffer.from(data));
}

function boolToInt(v) {
  return v === true || v === 1 || v === '1' || v === 'true' ? 1 : 0;
}
function intToBool(v) {
  return v === 1 || v === true;
}

function mapEstablecimiento(row) {
  if (!row) return null;
  return {
    id: row.id,
    nombre: row.nombre,
    ruc: row.ruc,
    direccion: row.direccion,
    telefono: row.telefono,
    ubigeo: row.ubigeo,
    actividad: row.actividad,
    nroRegistro: row.nroRegistro,
    fechaEmision: row.fechaEmision,
    placaPrincipal: row.placaPrincipal,
    sincronizado: intToBool(row.sincronizado)
  };
}

function mapFiscalizacion(row) {
  if (!row) return null;
  return {
    id: row.id,
    establecimientoId: row.establecimientoId,
    expediente: row.expediente,
    agenteFiscalizado: row.agenteFiscalizado,
    codigoOsinergmin: row.codigoOsinergmin,
    registroHidrocarburos: row.registroHidrocarburos,
    fechaDiligencia: row.fechaDiligencia,
    horaApertura: row.horaApertura,
    horaCierre: row.horaCierre,
    direccion: row.direccion,
    distrito: row.distrito,
    provincia: row.provincia,
    departamento: row.departamento,
    rucDni: row.rucDni,
    telefonoFax: row.telefonoFax,
    fiscalizadorResponsable: row.fiscalizadorResponsable,
    productosJson: row.productosJson,
    telefonoPublicado: row.telefonoPublicado,
    telefonoActualizado: row.telefonoActualizado,
    horarioPublicado: row.horarioPublicado,
    listaPreciosExhibida: row.listaPreciosExhibida,
    unidadGalonEmpleada: row.unidadGalonEmpleada,
    etiquetaVisible: row.etiquetaVisible,
    incumplimientosJson: row.incumplimientosJson,
    hechosVerificados: row.hechosVerificados,
    documentacion: row.documentacion,
    ocurrencias: row.ocurrencias,
    observaciones: row.observaciones,
    negativaFirma: intToBool(row.negativaFirma),
    firmaInspectorPath: row.firmaInspectorPath,
    firmaResponsablePath: row.firmaResponsablePath,
    sincronizado: intToBool(row.sincronizado),
    estado: row.estado,
    latitud: row.latitud,
    longitud: row.longitud,
    fotosJson: row.fotosJson,
    historialCambios: row.historialCambios
  };
}

function rowsFrom(stmt) {
  const cols = stmt.getColumnNames();
  const out = [];
  while (stmt.step()) {
    const values = stmt.get();
    const obj = {};
    cols.forEach((c, i) => { obj[c] = values[i]; });
    out.push(obj);
  }
  stmt.free();
  return out;
}

function oneFrom(sql, params = []) {
  const stmt = db.prepare(sql);
  stmt.bind(params);
  const rows = rowsFrom(stmt);
  return rows[0] || null;
}

function allFrom(sql, params = []) {
  const stmt = db.prepare(sql);
  stmt.bind(params);
  return rowsFrom(stmt);
}

function run(sql, params = []) {
  db.run(sql, params);
  persist();
}

async function boot() {
  const SQL = await initSqlJs();
  if (fs.existsSync(dbPath)) {
    const fileBuffer = fs.readFileSync(dbPath);
    db = new SQL.Database(fileBuffer);
  } else {
    db = new SQL.Database();
    const schema = fs.readFileSync(schemaPath, 'utf8');
    db.run(schema);
    persist();
  }

  // Asegura tablas mínimas si el archivo existía vacío
  const tables = allFrom("SELECT name FROM sqlite_master WHERE type='table' AND name='establecimientos'");
  if (tables.length === 0) {
    const schema = fs.readFileSync(schemaPath, 'utf8');
    db.run(schema);
    persist();
  }

  const app = express();
  app.use(cors());
  app.use(express.json({ limit: '40mb' }));

  // Tokens emitidos en login (en memoria). En producción usar JWT firmado / Redis.
  const tokensActivos = new Map(); // token -> { username, nombre, rol, creado }

  function requireAuth(req, res, next) {
    const header = req.headers.authorization || '';
    const match = header.match(/^Bearer\s+(.+)$/i);
    if (!match) {
      return res.status(401).json({ error: 'Falta token. Use Authorization: Bearer <token>' });
    }
    const token = match[1].trim();
    const session = tokensActivos.get(token);
    if (!session) {
      return res.status(401).json({ error: 'Token inválido o expirado. Vuelva a iniciar sesión.' });
    }
    req.user = session;
    next();
  }

  app.get('/', (_req, res) => {
    res.json({
      name: 'Acta PRICE API',
      version: '1.0.0',
      status: 'ok',
      auth: 'Bearer token requerido en /establecimientos y /fiscalizaciones',
      endpoints: [
        'POST /auth/login',
        'GET /establecimientos (auth)',
        'POST /establecimientos (auth)',
        'GET /fiscalizaciones (auth)',
        'POST /fiscalizaciones (auth)',
        'POST /documentos/docx-to-pdf'
      ]
    });
  });

  app.post('/auth/login', (req, res) => {
    const { username, password } = req.body || {};
    if (!username || !password) {
      return res.status(400).json({ token: null, error: 'username y password son obligatorios' });
    }
    const user = oneFrom(
      'SELECT * FROM usuarios WHERE username = ? AND password = ?',
      [username, password]
    );
    if (!user) {
      return res.status(401).json({ token: null, error: 'Credenciales inválidas' });
    }
    const token = 'jwt-' + uuidv4();
    tokensActivos.set(token, {
      username: user.username,
      nombre: user.nombre,
      rol: user.rol,
      creado: Date.now()
    });
    return res.json({
      token,
      error: null,
      nombre: user.nombre,
      rol: user.rol
    });
  });

  // Ruta de prueba: sin token debe fallar
  app.get('/establecimientos', requireAuth, (_req, res) => {
    const rows = allFrom('SELECT * FROM establecimientos ORDER BY id DESC');
    res.json(rows.map(mapEstablecimiento));
  });

  app.post('/establecimientos', requireAuth, (req, res) => {
    const e = req.body || {};
    run(
      `INSERT INTO establecimientos
        (nombre, ruc, direccion, telefono, ubigeo, actividad, nroRegistro, fechaEmision, placaPrincipal, sincronizado)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1)`,
      [
        e.nombre || null,
        e.ruc || null,
        e.direccion || null,
        e.telefono || null,
        e.ubigeo || null,
        e.actividad || null,
        e.nroRegistro || null,
        e.fechaEmision || null,
        e.placaPrincipal || null
      ]
    );
    const id = db.exec('SELECT last_insert_rowid() as id')[0].values[0][0];
    const created = oneFrom('SELECT * FROM establecimientos WHERE id = ?', [id]);
    res.status(201).json(mapEstablecimiento(created));
  });

  app.get('/fiscalizaciones', requireAuth, (_req, res) => {
    const rows = allFrom('SELECT * FROM fiscalizaciones ORDER BY id DESC');
    res.json(rows.map(mapFiscalizacion));
  });

  app.post('/fiscalizaciones', requireAuth, (req, res) => {
    const f = req.body || {};
    if (f.establecimientoId == null) {
      return res.status(400).json({ error: 'establecimientoId es obligatorio' });
    }
    run(
      `INSERT INTO fiscalizaciones (
        establecimientoId, expediente, agenteFiscalizado, codigoOsinergmin, registroHidrocarburos,
        fechaDiligencia, horaApertura, horaCierre, direccion, distrito, provincia, departamento,
        rucDni, telefonoFax, fiscalizadorResponsable, productosJson,
        telefonoPublicado, telefonoActualizado, horarioPublicado, listaPreciosExhibida,
        unidadGalonEmpleada, etiquetaVisible, incumplimientosJson, hechosVerificados,
        documentacion, ocurrencias, observaciones, negativaFirma, firmaInspectorPath, firmaResponsablePath,
        sincronizado, estado, latitud, longitud, fotosJson, historialCambios
      ) VALUES (
        ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1,?,?,?,?,?
      )`,
      [
        f.establecimientoId,
        f.expediente || null,
        f.agenteFiscalizado || null,
        f.codigoOsinergmin || null,
        f.registroHidrocarburos || null,
        f.fechaDiligencia || null,
        f.horaApertura || null,
        f.horaCierre || null,
        f.direccion || null,
        f.distrito || null,
        f.provincia || null,
        f.departamento || null,
        f.rucDni || null,
        f.telefonoFax || null,
        f.fiscalizadorResponsable || null,
        f.productosJson || null,
        f.telefonoPublicado || null,
        f.telefonoActualizado || null,
        f.horarioPublicado || null,
        f.listaPreciosExhibida || null,
        f.unidadGalonEmpleada || null,
        f.etiquetaVisible || null,
        f.incumplimientosJson || null,
        f.hechosVerificados || null,
        f.documentacion || null,
        f.ocurrencias || null,
        f.observaciones || null,
        boolToInt(f.negativaFirma),
        f.firmaInspectorPath || null,
        f.firmaResponsablePath || null,
        f.estado || 'ACTA GENERADA',
        f.latitud || 0,
        f.longitud || 0,
        f.fotosJson || null,
        f.historialCambios || null
      ]
    );
    const id = db.exec('SELECT last_insert_rowid() as id')[0].values[0][0];
    const created = oneFrom('SELECT * FROM fiscalizaciones WHERE id = ?', [id]);
    res.status(201).json(mapFiscalizacion(created));
  });

  /**
   * Convierte DOCX → PDF con Microsoft Word (COM) en el PC.
   * Body: { docxBase64, fileName? }
   * Resp: { pdfBase64, pages? } | { error }
   */
  app.post('/documentos/docx-to-pdf', async (req, res) => {
    const b64 = req.body && req.body.docxBase64;
    if (!b64 || typeof b64 !== 'string') {
      return res.status(400).json({ error: 'docxBase64 requerido' });
    }

    const id = uuidv4();
    const docxPath = path.join(tmpDir, `in_${id}.docx`);
    const pdfPath = path.join(tmpDir, `out_${id}.pdf`);
    const script = path.join(scriptsDir, 'docx2pdf.ps1');

    try {
      if (!fs.existsSync(script)) {
        return res.status(500).json({ error: `Falta script ${script}` });
      }
      fs.writeFileSync(docxPath, Buffer.from(b64, 'base64'));

      await execFileAsync(
        'powershell.exe',
        ['-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', script, '-DocxPath', docxPath, '-PdfPath', pdfPath],
        { windowsHide: true, timeout: 120000, maxBuffer: 10 * 1024 * 1024 }
      );

      if (!fs.existsSync(pdfPath) || fs.statSync(pdfPath).size < 100) {
        return res.status(500).json({ error: 'Word no generó el PDF' });
      }

      const pdfBase64 = fs.readFileSync(pdfPath).toString('base64');
      res.json({ pdfBase64, error: null });
    } catch (err) {
      console.error('docx-to-pdf error', err);
      res.status(500).json({
        error: err.message || 'Error convirtiendo DOCX a PDF',
        detail: String(err.stderr || err.stdout || '')
      });
    } finally {
      try { if (fs.existsSync(docxPath)) fs.unlinkSync(docxPath); } catch (_) {}
      try { if (fs.existsSync(pdfPath)) fs.unlinkSync(pdfPath); } catch (_) {}
    }
  });

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Acta PRICE API escuchando en http://0.0.0.0:${PORT}`);
    console.log(`LAN: http://192.168.0.10:${PORT}`);
    console.log(`SQLite: ${dbPath}`);
  });
}

boot().catch((err) => {
  console.error('No se pudo iniciar la API', err);
  process.exit(1);
});
