package com.example.actapriceproyect.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.example.actapriceproyect.di.NetworkModule;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Llena la plantilla oficial con Apache POI (sin límite de Aspose)
 * y convierte a PDF en el PC vía API + Microsoft Word (2 páginas fieles).
 */
public class WordGenerator {

    private static final String TAG = "WordGenerator";

    public static class Resultado {
        public final File docx;
        public final File pdf;
        public final int paginasPdf;

        public Resultado(File docx, File pdf, int paginasPdf) {
            this.docx = docx;
            this.pdf = pdf;
            this.paginasPdf = paginasPdf;
        }
    }

    public static File[] generarDocumentosFinales(Context context, String templateName,
                                                  Map<String, String> datos,
                                                  Map<String, String> pathsFirmas,
                                                  List<Map<String, String>> listaProductos,
                                                  List<Map<String, String>> listaHechos) {
        Resultado r = generar(context, templateName, datos, pathsFirmas, listaProductos, listaHechos);
        if (r == null) return null;
        return new File[]{r.docx, r.pdf};
    }

    public static Resultado generar(Context context, String templateName,
                                    Map<String, String> datos,
                                    Map<String, String> pathsFirmas,
                                    List<Map<String, String>> listaProductos,
                                    List<Map<String, String>> listaHechos) {

        long timestamp = System.currentTimeMillis();
        File docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (docsDir != null && !docsDir.exists()) docsDir.mkdirs();

        File docxFile = new File(docsDir, "Acta_PRICE_" + timestamp + ".docx");
        File pdfFile = new File(docsDir, "Acta_PRICE_" + timestamp + ".pdf");

        try {
            Log.e(TAG, "=== INICIO generar (POI + Word API) ===");

            Map<String, String> safeDatos = new HashMap<>();
            if (datos != null) {
                for (Map.Entry<String, String> e : datos.entrySet()) {
                    safeDatos.put(e.getKey(), e.getValue() != null ? e.getValue() : "");
                }
            }

            String asset = resolverPlantilla(context, templateName);
            Log.e(TAG, "Plantilla: " + asset);

            try (InputStream in = context.getAssets().open(asset);
                 XWPFDocument doc = new XWPFDocument(in)) {

                reemplazarPlaceholders(doc, safeDatos);
                llenarTablaPrecios(doc, listaProductos);
                llenarTablaHechos(doc, listaHechos);
                pegarFirmasEnTabla(doc, pathsFirmas);
                // Quitar placeholders sueltos de firma (están fuera de la tabla de firmas)
                replaceInDocument(doc, "{{ firma_f }}", "");
                replaceInDocument(doc, "{{ firma_r }}", "");
                replaceInDocument(doc, "{{firma_f}}", "");
                replaceInDocument(doc, "{{firma_r}}", "");
                limpiarPlaceholdersRestantes(doc);

                try (FileOutputStream fos = new FileOutputStream(docxFile)) {
                    doc.write(fos);
                }
            }

            Log.e(TAG, "DOCX OK bytes=" + docxFile.length() + " -> " + docxFile.getName());

            boolean pdfOk = convertirPdfConWordApi(docxFile, pdfFile);
            int paginas = pdfOk ? contarPaginasPdf(pdfFile) : 0;
            Log.e(TAG, "PDF via Word API ok=" + pdfOk + " paginas=" + paginas
                    + " bytes=" + (pdfFile.exists() ? pdfFile.length() : 0));

            if (!pdfOk || paginas < 1) {
                throw new IllegalStateException(
                        "No se pudo convertir a PDF. ¿API + Word corriendo en el PC?");
            }

            return new Resultado(docxFile, pdfFile, paginas);

        } catch (Throwable e) {
            Log.e(TAG, "Error generando documentos: " + e.getMessage(), e);
            return null;
        }
    }

    /** Envía el DOCX a la API; el PC lo abre con Microsoft Word y exporta PDF. */
    private static boolean convertirPdfConWordApi(File docx, File pdfOut) {
        HttpURLConnection conn = null;
        try {
            byte[] bytes = leerArchivo(docx);
            String b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
            JSONObject body = new JSONObject();
            body.put("docxBase64", b64);
            body.put("fileName", docx.getName());

            URL url = new URL(NetworkModule.getBaseUrl() + "documentos/docx-to-pdf");
            Log.e(TAG, "POST " + url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(120000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);
            byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(payload.length);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
            }

            int code = conn.getResponseCode();
            InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
            byte[] resp = leerStream(stream);
            if (code < 200 || code >= 300) {
                Log.e(TAG, "API PDF error HTTP " + code + " " + new String(resp, StandardCharsets.UTF_8));
                return false;
            }

            JSONObject json = new JSONObject(new String(resp, StandardCharsets.UTF_8));
            String pdfB64 = json.optString("pdfBase64", "");
            if (pdfB64.isEmpty()) {
                Log.e(TAG, "API no devolvió pdfBase64: " + json.optString("error"));
                return false;
            }
            byte[] pdfBytes = Base64.decode(pdfB64, Base64.DEFAULT);
            try (FileOutputStream fos = new FileOutputStream(pdfOut)) {
                fos.write(pdfBytes);
            }
            return pdfOut.exists() && pdfOut.length() > 100;
        } catch (Exception e) {
            Log.e(TAG, "convertirPdfConWordApi falló", e);
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static void reemplazarPlaceholders(XWPFDocument doc, Map<String, String> datos) {
        for (Map.Entry<String, String> e : datos.entrySet()) {
            replaceInDocument(doc, "{{" + e.getKey() + "}}", e.getValue());
        }
        // Tags rotos de la plantilla original
        replaceInDocument(doc, "{{documentacion}", safe(datos.get("documentacion")));
        replaceInDocument(doc, "{{manifestaciones}", safe(datos.get("manifestaciones")));
        replaceInDocument(doc, "{{ documentacion }}", safe(datos.get("documentacion")));
    }

    private static void limpiarPlaceholdersRestantes(XWPFDocument doc) {
        String[] leftovers = {
                "{{ocurrencias}}", "{{documentacion}}", "{{manifestaciones}}", "{{negativa}}",
                "{{fis_dni}}", "{{fis_nombres}}", "{{rec_dni}}", "{{rec_nombres}}", "{{rec_relacion}}",
                "{{firma_f}}", "{{firma_r}}", "{{agente}}", "{{codigo}}", "{{registro}}"
        };
        for (String t : leftovers) replaceInDocument(doc, t, "");
    }

    private static void replaceInDocument(XWPFDocument doc, String find, String replace) {
        if (find == null || find.isEmpty()) return;
        String rep = replace != null ? replace : "";
        for (XWPFParagraph p : doc.getParagraphs()) replaceInParagraph(p, find, rep);
        for (XWPFTable table : doc.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) replaceInParagraph(p, find, rep);
                }
            }
        }
        doc.getHeaderList().forEach(h -> {
            for (XWPFParagraph p : h.getParagraphs()) replaceInParagraph(p, find, rep);
            for (XWPFTable table : h.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) replaceInParagraph(p, find, rep);
                    }
                }
            }
        });
        doc.getFooterList().forEach(f -> {
            for (XWPFParagraph p : f.getParagraphs()) replaceInParagraph(p, find, rep);
        });
    }

    /** Reemplazo tolerante a placeholders partidos en varios runs. */
    private static void replaceInParagraph(XWPFParagraph paragraph, String find, String replace) {
        String full = paragraph.getText();
        if (full == null || !full.contains(find)) return;

        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) return;

        // Estrategia simple: juntar, reemplazar y reescribir en el primer run
        StringBuilder sb = new StringBuilder();
        for (XWPFRun run : runs) {
            String t = run.getText(0);
            if (t != null) sb.append(t);
        }
        String updated = sb.toString().replace(find, replace);
        if (updated.equals(sb.toString())) return;

        for (int i = runs.size() - 1; i >= 1; i--) {
            paragraph.removeRun(i);
        }
        XWPFRun first = paragraph.getRuns().isEmpty() ? paragraph.createRun() : paragraph.getRuns().get(0);
        first.setText(updated, 0);
    }

    private static void llenarTablaPrecios(XWPFDocument doc, List<Map<String, String>> productos) {
        if (productos == null || productos.isEmpty()) return;
        XWPFTable table = buscarTabla(doc, "Producto Fiscalizado");
        if (table == null) {
            Log.e(TAG, "Tabla precios no encontrada");
            return;
        }

        Map<String, Integer> colPorProducto = new HashMap<>();
        colPorProducto.put("diesel b5 s-50", 1);
        colPorProducto.put("gasohol 84 plus", 2);
        colPorProducto.put("gasohol regular", 3);
        colPorProducto.put("gasohol premium", 4);
        colPorProducto.put("glp automotor", 5);
        colPorProducto.put("otros", 6);
        colPorProducto.put("marca propia / otros", 6);
        colPorProducto.put("glp envasado 3 kg", 8);
        colPorProducto.put("glp envasado 5 kg", 9);
        colPorProducto.put("glp envasado 10 kg", 10);
        colPorProducto.put("glp envasado 15 kg", 11);
        colPorProducto.put("glp envasado 45 kg", 12);

        final int ROW_PRICE = 2, ROW_PUB = 5, ROW_SUR = 8, ROW_DESC = 11;
        final int COL_MARCA = 7, ROW_MARCA = 1;

        for (Map<String, String> p : productos) {
            String nombre = p.get("p_nom");
            if (nombre == null) continue;
            String key = nombre.trim().toLowerCase(Locale.ROOT);
            Integer col = colPorProducto.get(key);
            if (col == null) {
                for (Map.Entry<String, Integer> e : colPorProducto.entrySet()) {
                    if (key.contains(e.getKey()) || e.getKey().contains(key)) {
                        col = e.getValue();
                        break;
                    }
                }
            }
            if (col == null) continue;

            setCelda(table, ROW_PRICE, col, safe(p.get("p_price")));
            setCelda(table, ROW_PUB, col, safe(p.get("p_pub")));
            setCelda(table, ROW_SUR, col, safe(p.get("p_sur")));
            setCelda(table, ROW_DESC, col, safe(p.get("p_desc")));

            String marca = safe(p.get("p_marca"));
            if (!marca.isEmpty() && (col == 6 || key.contains("otros"))) {
                setCelda(table, ROW_MARCA, COL_MARCA, marca);
            }
        }
    }

    private static void llenarTablaHechos(XWPFDocument doc, List<Map<String, String>> hechos) {
        if (hechos == null || hechos.isEmpty()) {
            Log.e(TAG, "Sin hechos verificados que volcar al acta");
            return;
        }
        XWPFTable table = buscarTabla(doc, "INCUMPLIMIENTO");
        if (table == null) table = buscarTabla(doc, "HECHOS VERIFICADOS");
        if (table == null) {
            Log.e(TAG, "Tabla hechos no encontrada");
            return;
        }

        // Filas del acta oficial: ítems 1,2,3,4,5,6
        int[] filasInc = {1, 2, 4, 5, 7, 8};

        for (Map<String, String> h : hechos) {
            String inc = safe(h.get("h_inc"));
            String red = safe(h.get("h_red"));
            if (red.isEmpty()) continue;
            int num = extraerNumeroIncumplimiento(inc);
            if (num < 1 || num > 6) {
                Log.e(TAG, "Hecho ignorado (no es incumplimiento 1-6): " + inc);
                continue;
            }
            int rowIdx = filasInc[num - 1];
            String actual = getCelda(table, rowIdx, 2).replace("_", "").trim();
            String nuevo = actual.isEmpty() ? red : actual + "\n" + red;
            setCelda(table, rowIdx, 2, nuevo);
            Log.e(TAG, "Hecho colocado en incumplimiento " + num + " (fila " + rowIdx + ")");
        }
    }

    /**
     * Inserta firmas en la tabla oficial:
     * col 0 = Fiscalizador, col 1 = Quien recibe.
     */
    private static void pegarFirmasEnTabla(XWPFDocument doc, Map<String, String> pathsFirmas) {
        if (pathsFirmas == null) return;
        XWPFTable table = buscarTabla(doc, "Firma del Fiscalizador");
        if (table == null) table = buscarTabla(doc, "Firma de quien recibe");
        if (table == null || table.getNumberOfRows() < 1) {
            Log.e(TAG, "Tabla de firmas no encontrada");
            return;
        }

        String pathF = pathsFirmas.get("firma_f");
        String pathR = pathsFirmas.get("firma_r");
        Log.e(TAG, "Firmas paths f=" + pathF + " r=" + pathR
                + " filasTabla=" + table.getNumberOfRows());

        // Fila 0: zona de la rúbrica (puntos); si no, crear párrafo en esa celda
        insertarFirmaEnCelda(table, 0, 0, pathF, "fiscalizador");
        insertarFirmaEnCelda(table, 0, 1, pathR, "receptor");
    }

    private static void insertarFirmaEnCelda(XWPFTable table, int row, int col, String path, String label) {
        if (path == null || path.trim().isEmpty()) {
            Log.e(TAG, "Sin path para firma " + label);
            return;
        }
        File img = new File(path);
        if (!img.exists()) {
            Log.e(TAG, "No existe archivo firma " + label + ": " + path);
            return;
        }
        if (row >= table.getNumberOfRows()) return;
        XWPFTableRow r = table.getRow(row);
        if (r == null || col >= r.getTableCells().size()) return;
        XWPFTableCell cell = r.getCell(col);
        if (cell == null) return;

        try {
            // Limpiar puntos / texto previo de la celda
            for (int i = cell.getParagraphs().size() - 1; i >= 0; i--) {
                cell.removeParagraph(i);
            }
            XWPFParagraph p = cell.addParagraph();
            XWPFRun run = p.createRun();
            try (FileInputStream fis = new FileInputStream(img)) {
                String name = img.getName().toLowerCase(Locale.ROOT);
                int format = name.endsWith(".png") ? XWPFDocument.PICTURE_TYPE_PNG : XWPFDocument.PICTURE_TYPE_JPEG;
                run.addPicture(fis, format, img.getName(), Units.toEMU(150), Units.toEMU(60));
            }
            Log.e(TAG, "Firma " + label + " insertada en celda [" + row + "," + col + "]");
        } catch (Exception e) {
            Log.e(TAG, "Error insertando firma " + label, e);
        }
    }

    private static XWPFTable buscarTabla(XWPFDocument doc, String texto) {
        String needle = texto.toLowerCase(Locale.ROOT);
        for (XWPFTable table : doc.getTables()) {
            StringBuilder sb = new StringBuilder();
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    sb.append(cell.getText()).append(' ');
                }
            }
            if (sb.toString().toLowerCase(Locale.ROOT).contains(needle)) return table;
        }
        return null;
    }

    private static void setCelda(XWPFTable table, int row, int col, String text) {
        if (row >= table.getNumberOfRows()) return;
        XWPFTableRow r = table.getRow(row);
        if (r == null || col >= r.getTableCells().size()) return;
        XWPFTableCell cell = r.getCell(col);
        if (cell == null) return;
        // Limpiar y escribir
        for (int i = cell.getParagraphs().size() - 1; i >= 0; i--) {
            cell.removeParagraph(i);
        }
        XWPFParagraph p = cell.addParagraph();
        XWPFRun run = p.createRun();
        run.setFontSize(8);
        run.setText(text != null ? text : "");
    }

    private static String getCelda(XWPFTable table, int row, int col) {
        if (row >= table.getNumberOfRows()) return "";
        XWPFTableRow r = table.getRow(row);
        if (r == null || col >= r.getTableCells().size()) return "";
        XWPFTableCell cell = r.getCell(col);
        return cell != null ? cell.getText() : "";
    }

    private static int extraerNumeroIncumplimiento(String texto) {
        if (texto == null) return -1;
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("Incumplimiento\\s*(\\d)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(texto);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    private static String resolverPlantilla(Context context, String preferred) {
        String[] candidatos = { preferred, "plantilla.docx" };
        for (String name : candidatos) {
            if (name == null || name.trim().isEmpty()) continue;
            try (InputStream is = context.getAssets().open(name)) {
                if (is != null) return name;
            } catch (Exception ignored) {}
        }
        return "plantilla.docx";
    }

    private static int contarPaginasPdf(File pdf) {
        try (PdfDocument doc = new PdfDocument(new PdfReader(pdf.getAbsolutePath()))) {
            return doc.getNumberOfPages();
        } catch (Exception e) {
            Log.w(TAG, "No se pudo contar páginas", e);
            return 1;
        }
    }

    private static byte[] leerArchivo(File f) throws Exception {
        try (FileInputStream in = new FileInputStream(f)) {
            return leerStream(in);
        }
    }

    private static byte[] leerStream(InputStream in) throws Exception {
        if (in == null) return new byte[0];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) >= 0) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    private static String safe(String v) { return v != null ? v : ""; }
}
