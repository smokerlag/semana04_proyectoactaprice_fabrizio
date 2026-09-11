package com.example.actapriceproyect.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.aspose.words.*;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Llena la plantilla DOCX y exporta DOCX + PDF.
 * La página 2 (III. OTROS + firmas) se garantiza partiendo el doc y uniendo PDFs.
 */
public class WordGenerator {

    private static final String TAG = "WordGenerator";

    /** Resultado con archivos y número real de páginas del PDF. */
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
            File tempTemplate = copiarPlantilla(context, templateName);
            Document doc = new Document(tempTemplate.getAbsolutePath());
            DocumentBuilder builder = new DocumentBuilder(doc);

            Map<String, String> safeDatos = new HashMap<>();
            if (datos != null) {
                for (Map.Entry<String, String> e : datos.entrySet()) {
                    safeDatos.put(e.getKey(), e.getValue() != null ? e.getValue() : "");
                }
            }

            FindReplaceOptions opts = new FindReplaceOptions();
            for (Map.Entry<String, String> entry : safeDatos.entrySet()) {
                doc.getRange().replace("{{" + entry.getKey() + "}}", entry.getValue(), opts);
            }
            doc.getRange().replace("{{documentacion}", safeDatos.getOrDefault("documentacion", ""), opts);
            doc.getRange().replace("{{manifestaciones}", safeDatos.getOrDefault("manifestaciones", ""), opts);

            llenarTablaPrecios(doc, listaProductos);
            llenarTablaHechos(doc, listaHechos);

            pegarFirmaEnWord(doc, builder, "{{firma_f}}", pathsFirmas != null ? pathsFirmas.get("firma_f") : null);
            pegarFirmaEnWord(doc, builder, "{{firma_r}}", pathsFirmas != null ? pathsFirmas.get("firma_r") : null);

            doc.getRange().replace(java.util.regex.Pattern.compile("\\{\\{[^}]+\\}\\}"), "", opts);

            // Marca el inicio de página 2 en el propio párrafo (Word nativo)
            Paragraph anclaPag2 = marcarInicioPagina2(doc);

            doc.updatePageLayout();
            doc.save(docxFile.getAbsolutePath(), SaveFormat.DOCX);

            // PDF garantizado a 2 páginas si existe ancla (III. OTROS / firmas)
            int paginas = guardarPdfConPagina2(doc, anclaPag2, pdfFile, docsDir, timestamp);

            Log.i(TAG, "OK docx=" + docxFile.getName() + " pdfPaginas=" + paginas);
            return new Resultado(docxFile, pdfFile, paginas);

        } catch (Exception e) {
            Log.e(TAG, "Error generando documentos", e);
            return null;
        }
    }

    /**
     * Si Aspose deja todo en 1 página, parte el documento en dos y une los PDF con iText.
     */
    private static int guardarPdfConPagina2(Document doc, Paragraph anclaPag2,
                                            File pdfFinal, File docsDir, long ts) throws Exception {
        File pdfDirecto = new File(docsDir, "tmp_direct_" + ts + ".pdf");
        doc.save(pdfDirecto.getAbsolutePath(), SaveFormat.PDF);
        int pages = contarPaginasPdf(pdfDirecto);

        if (pages >= 2 || anclaPag2 == null) {
            if (pdfFinal.exists()) pdfFinal.delete();
            if (!pdfDirecto.renameTo(pdfFinal)) {
                copiarArchivo(pdfDirecto, pdfFinal);
                pdfDirecto.delete();
            }
            return Math.max(pages, 1);
        }

        // Forzar 2 páginas: partir en ancla
        int idx = indiceHijoBodyQueContiene(doc, anclaPag2);
        if (idx < 0) {
            if (!pdfDirecto.renameTo(pdfFinal)) {
                copiarArchivo(pdfDirecto, pdfFinal);
                pdfDirecto.delete();
            }
            return pages;
        }

        Document parte1 = (Document) doc.deepClone(true);
        Document parte2 = (Document) doc.deepClone(true);
        recortarBodyHasta(parte1, idx, true);  // deja [0 .. idx)
        recortarBodyHasta(parte2, idx, false); // deja [idx .. end)

        File pdf1 = new File(docsDir, "tmp_p1_" + ts + ".pdf");
        File pdf2 = new File(docsDir, "tmp_p2_" + ts + ".pdf");
        parte1.save(pdf1.getAbsolutePath(), SaveFormat.PDF);
        parte2.save(pdf2.getAbsolutePath(), SaveFormat.PDF);

        unirPdfs(pdf1, pdf2, pdfFinal);

        pdfDirecto.delete();
        pdf1.delete();
        pdf2.delete();

        int merged = contarPaginasPdf(pdfFinal);
        Log.i(TAG, "PDF forzado por split+merge, paginas=" + merged);
        return merged;
    }

    private static Paragraph marcarInicioPagina2(Document doc) throws Exception {
        Paragraph target = buscarParrafoFueraDeTabla(doc, "III", "OTROS");
        if (target == null) {
            // Si no hay III. OTROS, intenta LEYENDA (sigue siendo antes de firmas)
            target = buscarParrafoFueraDeTabla(doc, "LEYENDA", null);
        }
        if (target != null) {
            target.getParagraphFormat().setPageBreakBefore(true);
            Log.i(TAG, "pageBreakBefore=true en: " + safeTrim(target.getText()));
        } else {
            Log.w(TAG, "No se encontró ancla de página 2");
        }
        return target;
    }

    private static Paragraph buscarParrafoFueraDeTabla(Document doc, String a, String b) {
        NodeCollection paragraphs = doc.getChildNodes(NodeType.PARAGRAPH, true);
        for (Paragraph p : (Iterable<Paragraph>) paragraphs) {
            if (p.getAncestor(NodeType.TABLE) != null) continue;
            String t = p.getText();
            if (t == null) continue;
            String up = t.toUpperCase(Locale.ROOT);
            if (!up.contains(a.toUpperCase(Locale.ROOT))) continue;
            if (b != null && !up.contains(b.toUpperCase(Locale.ROOT))) continue;
            return p;
        }
        return null;
    }

    private static int indiceHijoBodyQueContiene(Document doc, Node nodo) {
        Body body = doc.getFirstSection().getBody();
        Node top = nodo;
        while (top != null && top.getParentNode() != null
                && top.getParentNode().getNodeType() != NodeType.BODY) {
            top = top.getParentNode();
        }
        if (top == null) return -1;
        NodeCollection kids = body.getChildNodes(NodeType.ANY, false);
        for (int i = 0; i < kids.getCount(); i++) {
            if (kids.get(i) == top) return i;
        }
        return -1;
    }

    /** keepHead=true: conserva [0..idx). keepHead=false: conserva [idx..fin). */
    private static void recortarBodyHasta(Document doc, int idx, boolean keepHead) {
        Body body = doc.getFirstSection().getBody();
        NodeCollection kids = body.getChildNodes(NodeType.ANY, false);
        if (keepHead) {
            for (int i = kids.getCount() - 1; i >= idx; i--) {
                kids.get(i).remove();
            }
        } else {
            for (int i = 0; i < idx; i++) {
                if (body.getFirstChild() != null) body.getFirstChild().remove();
            }
        }
    }

    private static void unirPdfs(File pdf1, File pdf2, File out) throws Exception {
        if (out.exists()) out.delete();
        PdfDocument dest = new PdfDocument(new PdfWriter(out.getAbsolutePath()));
        PdfDocument src1 = new PdfDocument(new PdfReader(pdf1.getAbsolutePath()));
        PdfDocument src2 = new PdfDocument(new PdfReader(pdf2.getAbsolutePath()));
        src1.copyPagesTo(1, src1.getNumberOfPages(), dest);
        src2.copyPagesTo(1, src2.getNumberOfPages(), dest);
        src1.close();
        src2.close();
        dest.close();
    }

    private static int contarPaginasPdf(File pdf) {
        try {
            PdfDocument doc = new PdfDocument(new PdfReader(pdf.getAbsolutePath()));
            int n = doc.getNumberOfPages();
            doc.close();
            return n;
        } catch (Exception e) {
            Log.w(TAG, "No se pudo contar páginas PDF", e);
            return 1;
        }
    }

    private static void copiarArchivo(File from, File to) throws Exception {
        try (InputStream in = new java.io.FileInputStream(from);
             java.io.FileOutputStream out = new java.io.FileOutputStream(to)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        }
    }

    private static File copiarPlantilla(Context context, String templateName) throws Exception {
        String assetName = resolverPlantilla(context, templateName);
        Log.i(TAG, "Plantilla: " + assetName);
        File tempTemplate = new File(context.getCacheDir(), "temp_template.docx");
        try (InputStream is = context.getAssets().open(assetName);
             java.io.FileOutputStream fos = new java.io.FileOutputStream(tempTemplate)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) fos.write(buffer, 0, read);
        }
        return tempTemplate;
    }

    private static void llenarTablaPrecios(Document doc, List<Map<String, String>> productos) throws Exception {
        if (productos == null || productos.isEmpty()) return;
        Table table = buscarTablaConTexto(doc, "Producto Fiscalizado");
        if (table == null) return;

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

            escribirCelda(table, ROW_PRICE, col, safe(p.get("p_price")));
            escribirCelda(table, ROW_PUB, col, safe(p.get("p_pub")));
            escribirCelda(table, ROW_SUR, col, safe(p.get("p_sur")));
            escribirCelda(table, ROW_DESC, col, safe(p.get("p_desc")));

            String marca = safe(p.get("p_marca"));
            if (!marca.isEmpty() && (col == 6 || key.contains("otros"))) {
                escribirCelda(table, ROW_MARCA, COL_MARCA, marca);
            }
        }
    }

    private static void llenarTablaHechos(Document doc, List<Map<String, String>> hechos) throws Exception {
        if (hechos == null || hechos.isEmpty()) return;
        Table table = buscarTablaConTexto(doc, "INCUMPLIMIENTO");
        if (table == null) return;

        int[] filasInc = {1, 2, 4, 5, 7, 8};
        StringBuilder extras = new StringBuilder();

        for (Map<String, String> h : hechos) {
            String inc = safe(h.get("h_inc"));
            String red = safe(h.get("h_red"));
            if (red.isEmpty()) continue;
            int num = extraerNumeroIncumplimiento(inc);
            if (num >= 1 && num <= 6) {
                int rowIdx = filasInc[num - 1];
                String actual = leerCelda(table, rowIdx, 2).replace("_", "").trim();
                String nuevo = actual.isEmpty() ? red : actual + "\n" + red;
                escribirCeldaConservandoAltura(table, rowIdx, 2, nuevo);
            } else {
                if (extras.length() > 0) extras.append("\n");
                extras.append(inc).append(": ").append(red);
            }
        }
        if (extras.length() > 0) {
            String actual = leerCelda(table, filasInc[0], 2).replace("_", "").trim();
            String nuevo = actual.isEmpty() ? extras.toString() : actual + "\n" + extras;
            escribirCeldaConservandoAltura(table, filasInc[0], 2, nuevo);
        }
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

    private static Table buscarTablaConTexto(Document doc, String texto) {
        NodeCollection tables = doc.getChildNodes(NodeType.TABLE, true);
        for (Table t : (Iterable<Table>) tables) {
            if (t.getText() != null && t.getText().contains(texto)) return t;
        }
        return null;
    }

    private static void escribirCelda(Table table, int row, int col, String text) throws Exception {
        if (row >= table.getRows().getCount()) return;
        Row r = table.getRows().get(row);
        if (col >= r.getCells().getCount()) return;
        Cell cell = r.getCells().get(col);
        Paragraph first = cell.getFirstParagraph();
        if (first == null) {
            first = new Paragraph(table.getDocument());
            cell.appendChild(first);
        }
        first.removeAllChildren();
        Run run = new Run(table.getDocument(), text != null ? text : "");
        run.getFont().setSize(8);
        first.appendChild(run);
    }

    private static void escribirCeldaConservandoAltura(Table table, int row, int col, String text) throws Exception {
        if (row >= table.getRows().getCount()) return;
        Row r = table.getRows().get(row);
        if (col >= r.getCells().getCount()) return;
        Cell cell = r.getCells().get(col);
        String value = text != null ? text : "";

        NodeCollection paragraphs = cell.getChildNodes(NodeType.PARAGRAPH, false);
        if (paragraphs.getCount() == 0) {
            Paragraph p = new Paragraph(table.getDocument());
            Run run = new Run(table.getDocument(), value);
            run.getFont().setSize(8);
            p.appendChild(run);
            cell.appendChild(p);
            return;
        }
        Paragraph first = (Paragraph) paragraphs.get(0);
        first.removeAllChildren();
        Run run = new Run(table.getDocument(), value);
        run.getFont().setSize(8);
        first.appendChild(run);
        // Deja el resto de párrafos (antes guiones) para no colapsar la fila
        for (int i = 1; i < paragraphs.getCount(); i++) {
            Paragraph p = (Paragraph) paragraphs.get(i);
            String pt = p.getText().replace("\u0007", "").replace("_", "").trim();
            if (pt.isEmpty()) p.removeAllChildren();
        }
    }

    private static String leerCelda(Table table, int row, int col) {
        if (row >= table.getRows().getCount()) return "";
        Row r = table.getRows().get(row);
        if (col >= r.getCells().getCount()) return "";
        return r.getCells().get(col).getText().replace("\u0007", "").trim();
    }

    private static String safe(String v) { return v != null ? v : ""; }

    private static String safeTrim(String v) {
        if (v == null) return "";
        String t = v.replace("\u0007", "").trim();
        return t.length() > 40 ? t.substring(0, 40) : t;
    }

    private static String resolverPlantilla(Context context, String preferred) {
        String[] candidatos = { preferred, "plantilla.docx", "plantilla_acta_filled.docx", "plantilla_acta.docx" };
        for (String name : candidatos) {
            if (name == null || name.trim().isEmpty()) continue;
            try (InputStream is = context.getAssets().open(name)) {
                if (is != null) return name;
            } catch (Exception ignored) {}
        }
        try {
            String[] root = context.getAssets().list("");
            if (root != null) {
                for (String f : root) {
                    if (f != null && f.toLowerCase(Locale.ROOT).endsWith(".docx")) return f;
                }
            }
        } catch (Exception ignored) {}
        return preferred != null ? preferred : "plantilla.docx";
    }

    private static void pegarFirmaEnWord(Document doc, DocumentBuilder builder, String tag, String path) throws Exception {
        if (path == null || !new File(path).exists()) {
            doc.getRange().replace(tag, " ", new FindReplaceOptions());
            return;
        }
        FindReplaceOptions opt = new FindReplaceOptions();
        opt.setReplacingCallback(args -> {
            builder.moveTo(args.getMatchNode());
            builder.insertImage(path, 120, 60);
            return ReplaceAction.REPLACE;
        });
        doc.getRange().replace(java.util.regex.Pattern.compile(java.util.regex.Pattern.quote(tag)), "", opt);
    }
}
