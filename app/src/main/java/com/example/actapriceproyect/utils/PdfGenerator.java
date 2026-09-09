package com.example.actapriceproyect.utils;

import android.content.Context;
import android.os.Environment;

import com.example.actapriceproyect.model.Establecimiento;
import com.example.actapriceproyect.model.Fiscalizacion;
import com.example.actapriceproyect.model.HechoVerificado;
import com.example.actapriceproyect.model.ProductoPrecio;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
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
            document.setMargins(30, 36, 30, 36);

            // --- PÁGINA 1: DATOS Y PRECIOS ---
            document.add(new Paragraph("ACTA DE FISCALIZACIÓN - FORMATO PRICE")
                    .setBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph("Expediente N°: " + fis.expediente)
                    .setBold().setTextAlignment(TextAlignment.RIGHT).setFontSize(10));

            // I. DATOS GENERALES
            document.add(new Paragraph("I. DATOS GENERALES").setBold().setFontSize(11).setMarginTop(10));
            Table tblGen = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            tblGen.addCell(new Cell().add(new Paragraph("Agente: " + fis.agenteFiscalizado).setFontSize(9)));
            tblGen.addCell(new Cell().add(new Paragraph("RUC/DNI: " + fis.rucDni).setFontSize(9)));
            tblGen.addCell(new Cell().add(new Paragraph("Código Osinergmin: " + fis.codigoOsinergmin).setFontSize(9)));
            tblGen.addCell(new Cell().add(new Paragraph("Registro Hidrocarburos: " + fis.registroHidrocarburos).setFontSize(9)));
            tblGen.addCell(new Cell().add(new Paragraph("Fecha: " + fis.fechaDiligencia).setFontSize(9)));
            tblGen.addCell(new Cell().add(new Paragraph("Hora: " + fis.horaApertura).setFontSize(9)));
            document.add(tblGen);

            // II. INFORMACIÓN DE PRECIOS
            document.add(new Paragraph("II. INFORMACIÓN DE PRECIOS RECABADA").setBold().setFontSize(11).setMarginTop(10));
            Table tblPrecios = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1, 1})).useAllAvailableWidth();
            tblPrecios.addHeaderCell(new Cell().add(new Paragraph("Producto").setBold().setFontSize(8)));
            tblPrecios.addHeaderCell(new Cell().add(new Paragraph("PRICE").setBold().setFontSize(8)));
            tblPrecios.addHeaderCell(new Cell().add(new Paragraph("Public.").setBold().setFontSize(8)));
            tblPrecios.addHeaderCell(new Cell().add(new Paragraph("Surtid.").setBold().setFontSize(8)));
            tblPrecios.addHeaderCell(new Cell().add(new Paragraph("Descuento").setBold().setFontSize(8)));
            
            if (fis.productosJson != null) {
                List<ProductoPrecio> productos = new Gson().fromJson(fis.productosJson, new TypeToken<List<ProductoPrecio>>(){}.getType());
                for (ProductoPrecio p : productos) {
                    tblPrecios.addCell(new Cell().add(new Paragraph(p.nombre).setFontSize(8)));
                    tblPrecios.addCell(new Cell().add(new Paragraph(p.precioPrice).setFontSize(8)));
                    tblPrecios.addCell(new Cell().add(new Paragraph(p.precioPublicado).setFontSize(8)));
                    tblPrecios.addCell(new Cell().add(new Paragraph(p.precioSurtidor).setFontSize(8)));
                    tblPrecios.addCell(new Cell().add(new Paragraph(p.precioDescuento).setFontSize(8)));
                }
            }
            document.add(tblPrecios);

            // III. SECCIÓN OTROS (VERIFICACIÓN)
            document.add(new Paragraph("III. SECCIÓN OTROS (VERIFICACIÓN)").setBold().setFontSize(11).setMarginTop(10));
            Table tblOtros = new Table(UnitValue.createPercentArray(new float[]{4, 1})).useAllAvailableWidth();
            tblOtros.addCell(new Cell().add(new Paragraph("Lista de Precios Vigente").setFontSize(9)));
            tblOtros.addCell(new Cell().add(new Paragraph("Sí".equals(fis.horarioPublicado) ? "CUMPLE" : "NO CUMPLE").setFontSize(9)));
            tblOtros.addCell(new Cell().add(new Paragraph("Teléfono registrado en PRICE").setFontSize(9)));
            tblOtros.addCell(new Cell().add(new Paragraph("Sí".equals(fis.telefonoActualizado) ? "CUMPLE" : "NO CUMPLE").setFontSize(9)));
            tblOtros.addCell(new Cell().add(new Paragraph("Unidad de Medida (Galón)").setFontSize(9)));
            tblOtros.addCell(new Cell().add(new Paragraph("CUMPLE").setFontSize(9))); // Por defecto en PRICE
            document.add(tblOtros);

            // FORZAR SEGUNDA PÁGINA
            document.add(new AreaBreak());

            // IV. HECHOS VERIFICADOS
            document.add(new Paragraph("IV. HECHOS VERIFICADOS (TRAZABILIDAD)").setBold().setFontSize(11));
            if (fis.hechosVerificados != null) {
                List<HechoVerificado> hechos = new Gson().fromJson(fis.hechosVerificados, new TypeToken<List<HechoVerificado>>(){}.getType());
                for (HechoVerificado h : hechos) {
                    document.add(new Paragraph("• " + h.incumplimientoNombre).setBold().setFontSize(9));
                    document.add(new Paragraph("  " + h.hechoRedactado).setFontSize(9).setItalic());
                }
            }

            // V. OBSERVACIONES Y FIRMAS
            document.add(new Paragraph("V. OBSERVACIONES").setBold().setFontSize(11).setMarginTop(10));
            document.add(new Paragraph(fis.observaciones != null ? fis.observaciones : "Sin observaciones.").setFontSize(9));

            if (fis.negativaFirma) {
                document.add(new Paragraph("\nNOTA: EL RESPONSABLE SE NEGÓ A FIRMAR EL ACTA.")
                        .setBold().setFontColor(new DeviceRgb(255, 0, 0)).setFontSize(10));
            }

            // PIE DE PÁGINA (FIRMAS)
            document.add(new Paragraph("\n\n\n\n__________________________          __________________________")
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Firma del Inspector                  Firma del Responsable")
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(9));

            document.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
