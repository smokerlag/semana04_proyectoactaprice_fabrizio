package com.example.actapriceproyect.utils;

import android.content.Context;
import android.os.Environment;
import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.util.List;
import java.util.Map;

public class WordGenerator {

    public static File generarWordDesdePlantilla(Context context, String templateName, String outputName, Map<String, String> datos) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), outputName);
        
        try (InputStream is = context.getAssets().open(templateName);
             XWPFDocument doc = new XWPFDocument(is);
             FileOutputStream out = new FileOutputStream(file)) {

            // Reemplazar en párrafos comunes
            for (XWPFParagraph p : doc.getParagraphs()) {
                reemplazarEtiquetas(p, datos);
            }

            // Reemplazar dentro de tablas (donde suelen estar los datos del acta)
            for (XWPFTable tbl : doc.getTables()) {
                for (XWPFTableRow row : tbl.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            reemplazarEtiquetas(p, datos);
                        }
                    }
                }
            }

            doc.write(out);
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void reemplazarEtiquetas(XWPFParagraph p, Map<String, String> datos) {
        List<XWPFRun> runs = p.getRuns();
        if (runs != null) {
            for (XWPFRun r : runs) {
                String text = r.getText(0);
                if (text != null) {
                    for (Map.Entry<String, String> entry : datos.entrySet()) {
                        if (text.contains("{{" + entry.getKey() + "}}")) {
                            text = text.replace("{{" + entry.getKey() + "}}", entry.getValue());
                            r.setText(text, 0);
                        }
                    }
                }
            }
        }
    }
}
