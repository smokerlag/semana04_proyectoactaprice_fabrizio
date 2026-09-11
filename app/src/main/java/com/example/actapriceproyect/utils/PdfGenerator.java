package com.example.actapriceproyect.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.actapriceproyect.model.Establecimiento;
import com.example.actapriceproyect.model.Fiscalizacion;
import com.example.actapriceproyect.model.HechoVerificado;
import com.example.actapriceproyect.model.ProductoPrecio;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PdfGenerator {

    public static File generarActaPrice(Context context, Establecimiento est, Fiscalizacion fis) {
        String fileName = "Acta_PRICE_" + (fis.expediente != null ? fis.expediente.replace("/", "_") : fis.id) + ".pdf";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(25, 30, 25, 30);

            // --- PÁGINA 1 ---
            
            // --- 1. ENCABEZADO OFICIAL ---
            Table tblHead = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
            tblHead.setBorder(Border.NO_BORDER);

            Cell cellInfo = new Cell().add(new Paragraph("Oficina Regional Huánuco\nDirección: Pasaje Mayro No 121\nTeléfono: 062 - 518499")
                    .setFontSize(8).setItalic()).setBorder(Border.NO_BORDER);
            tblHead.addCell(cellInfo);

            Cell cellExp = new Cell().add(new Paragraph("EXPEDIENTE Nro.\n" + (fis.expediente != null ? fis.expediente : "---"))
                    .setBold().setFontSize(10).setTextAlignment(TextAlignment.CENTER))
                    .setBorder(new SolidBorder(0.5f)).setPadding(5);
            tblHead.addCell(cellExp);
            document.add(tblHead);

            document.add(new Paragraph("\nActa de Fiscalización del Cumplimiento del Procedimiento de Entrega de Información de Precios de Combustibles Derivados de Hidrocarburos PRICE")
                    .setBold().setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginTop(10));

            // --- 2. TABLA I: DATOS GENERALES ---
            document.add(new Paragraph("I. DATOS DEL AGENTE Y DE LA DILIGENCIA").setBold().setFontSize(9).setMarginTop(10));
            Table tblI = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
            
            addTableRow(tblI, "AGENTE FISCALIZADO:", fis.agenteFiscalizado);
            addTableRow(tblI, "CÓDIGO OSINERGMIN:", fis.codigoOsinergmin);
            addTableRow(tblI, "REGISTRO HIDROCARBUROS:", fis.registroHidrocarburos);
            addTableRow(tblI, "FECHA DE DILIGENCIA:", fis.fechaDiligencia);
            addTableRow(tblI, "HORA DE APERTURA:", fis.horaApertura);
            addTableRow(tblI, "HORA DE CIERRE:", fis.horaCierre);
            addTableRow(tblI, "DIRECCIÓN:", fis.direccion);
            addTableRow(tblI, "UBICACIÓN:", (fis.departamento + " / " + fis.provincia + " / " + fis.distrito));
            
            document.add(tblI);

            // --- 3. TABLA II: CHECKLIST ---
            document.add(new Paragraph("II. VERIFICACIÓN DE OBLIGACIONES").setBold().setFontSize(9).setMarginTop(10));
            Table tblII = new Table(UnitValue.createPercentArray(new float[]{4, 1})).useAllAvailableWidth();
            addVerifRow(tblII, "1. ¿Teléfono publicado en el establecimiento?", fis.telefonoPublicado);
            addVerifRow(tblII, "2. ¿Teléfono registrado y actualizado en el PRICE?", fis.telefonoActualizado);
            addVerifRow(tblII, "3. ¿Horario de atención publicado en el establecimiento?", fis.horarioPublicado);
            document.add(tblII);

            // SALTO DE PÁGINA OBLIGATORIO PARA SEGUNDA PÁGINA
            document.add(new AreaBreak());

            // --- PÁGINA 2 ---

            // --- 4. TABLA III: HECHOS VERIFICADOS (PRECIOS Y BASE LEGAL) ---
            document.add(new Paragraph("III. HECHOS VERIFICADOS").setBold().setFontSize(9).setMarginTop(10));
            Table tblHechos = new Table(UnitValue.createPercentArray(new float[]{0.5f, 4, 3})).useAllAvailableWidth();
            tblHechos.addHeaderCell(createHeader("N°"));
            tblHechos.addHeaderCell(createHeader("INCUMPLIMIENTO / BASE LEGAL"));
            tblHechos.addHeaderCell(createHeader("HECHOS VERIFICADOS (TRAZABILIDAD)"));

            // Hechos Dinámicos de Precios
            String hechosPrecios = parseHechosPrecios(fis.productosJson);
            addHechoRow(tblHechos, "1", "No registra ni actualiza en el módulo PRICE el precio de venta vigente... Base Legal: Art. 3, 4, 6, 8, 14 y 15 de la R.C.D. N° 256-2021-OS/CD...", hechosPrecios);
            addHechoRow(tblHechos, "2", "No exhibe la lista de precios vigente... Base Legal: Art. 5, 8, 14 y 15 de la R.C.D. N° 256-2021-OS/CD...", "");
            addHechoRow(tblHechos, "3", "No registra ni actualiza en el módulo PRICE su ubicación o teléfono... Base Legal: Art. 8, 14 y 15 de la R.C.D. N° 256-2021-OS/CD...", "");
            addHechoRow(tblHechos, "4", "No exhibe el horario de atención ni número telefónico vigente en paneles... Base Legal: Art. 8, 14 y 15 de la R.C.D. N° 256-2021-OS/CD...", "");
            addHechoRow(tblHechos, "5", "No emplea el galón como unidad de medida... Base Legal: Art. 1 del D.S. N° 013-2021-EM.", "");
            addHechoRow(tblHechos, "6", "No coloca en la parte frontal de los dispensadores una etiqueta visible... Base Legal: Art. 1 del D.S. N° 013-2021-EM.", "");

            document.add(tblHechos);

            // --- 5. SECCIÓN OTROS ---
            document.add(new Paragraph("\nIV. OTROS").setBold().setFontSize(9));
            document.add(new Paragraph().add(new Text("Otras ocurrencias detectadas: ").setBold()).add(new Text(fis.ocurrencias != null ? fis.ocurrencias : "Ninguna")).setFontSize(8));
            document.add(new Paragraph().add(new Text("Documentación recabada: ").setBold()).add(new Text(fis.documentacion != null ? fis.documentacion : "Ninguna")).setFontSize(8));
            document.add(new Paragraph().add(new Text("Manifestaciones u observaciones del Agente: ").setBold()).add(new Text(fis.observaciones != null ? fis.observaciones : "Ninguna")).setFontSize(8));
            document.add(new Paragraph().add(new Text("Negativa a identificarse o firmar: ").setBold()).add(new Text(fis.negativaFirma ? "SÍ SE REGISTRA NEGATIVA" : "NO")).setFontSize(8));

            // --- 6. ÁREA DE FIRMAS ---
            document.add(new Paragraph("\n\n"));
            Table tblFirmas = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            tblFirmas.setBorder(Border.NO_BORDER);

            // Firma Fiscalizador
            Cell f1 = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
            if (fis.firmaInspectorPath != null && new File(fis.firmaInspectorPath).exists()) {
                f1.add(new Image(ImageDataFactory.create(fis.firmaInspectorPath)).setMaxWidth(100).setHeight(50));
            }
            f1.add(new Paragraph("__________________________\nFISCALIZADOR\nUZURIAGA CLAUDIO DARWIN\nDNI: 46060749").setFontSize(7));
            tblFirmas.addCell(f1);

            // Firma Receptor
            Cell f2 = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
            if (!fis.negativaFirma && fis.firmaResponsablePath != null && new File(fis.firmaResponsablePath).exists()) {
                f2.add(new Image(ImageDataFactory.create(fis.firmaResponsablePath)).setMaxWidth(100).setHeight(50));
            }
            f2.add(new Paragraph("__________________________\nPOR EL AGENTE FISCALIZADO\n(Nombre/DNI/Relación)").setFontSize(7));
            tblFirmas.addCell(f2);

            document.add(tblFirmas);

            // --- 7. PIE DE PÁGINA LEGAL ---
            document.add(new Paragraph("\nBASE LEGAL CONSOLIDADA: Texto único Ordenado de la Ley N° 27444, Ley N° 26734, Ley N° 27332, Ley N° 27699, D.S. N° 054-2001-PCM y R.C.D. N° 208-2020-OS/CD.")
                    .setFontSize(6).setTextAlignment(TextAlignment.JUSTIFIED).setMarginTop(10));

            document.close();
            return file;
        } catch (Exception e) {
            Log.e("PdfGenerator", "Error", e);
            return null;
        }
    }

    private static void addTableRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold().setFontSize(8)));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "").setFontSize(8)));
    }

    private static void addVerifRow(Table table, String question, String answer) {
        table.addCell(new Cell().add(new Paragraph(question).setFontSize(8)));
        table.addCell(new Cell().add(new Paragraph(answer != null ? answer : "---").setBold().setFontSize(8).setTextAlignment(TextAlignment.CENTER)));
    }

    private static Cell createHeader(String text) {
        return new Cell().add(new Paragraph(text).setBold().setFontSize(8)).setBackgroundColor(ColorConstants.LIGHT_GRAY);
    }

    private static void addHechoRow(Table table, String n, String incumplimiento, String hechos) {
        table.addCell(new Cell().add(new Paragraph(n).setFontSize(7)));
        table.addCell(new Cell().add(new Paragraph(incumplimiento).setFontSize(7)));
        table.addCell(new Cell().add(new Paragraph(hechos != null ? hechos : "").setFontSize(7).setItalic()));
    }

    private static String parseHechosPrecios(String json) {
        if (json == null) return "";
        try {
            List<ProductoPrecio> list = new Gson().fromJson(json, new TypeToken<List<ProductoPrecio>>(){}.getType());
            StringBuilder sb = new StringBuilder();
            for (ProductoPrecio p : list) {
                if (!p.precioPrice.isEmpty()) {
                    sb.append(p.nombre).append(": P.PRICE: ").append(p.precioPrice).append(" / P.SURTIDOR: ").append(p.precioSurtidor).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) { return ""; }
    }
}
