package com.example.actapriceproyect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.actapriceproyect.model.Fiscalizacion;
import com.example.actapriceproyect.model.HechoVerificado;
import com.example.actapriceproyect.model.ProductoPrecio;
import com.example.actapriceproyect.repository.ActaRepository;
import com.example.actapriceproyect.utils.WordGenerator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FiscalizacionPreviewActivity extends AppCompatActivity {

    private static final String TEMPLATE_NAME = "plantilla.docx";

    @Inject
    ActaRepository repository;

    private TextView tvResumen, tvErrores, tvEstadoDocs;
    private Button btnGenerarPdf, btnVerPdf, btnCompartirPdf, btnAbrirWord;
    private LinearLayout panelAcciones;
    private Fiscalizacion fis;
    private int establecimientoId;
    private Map<String, String> pathsFirmas = new HashMap<>();
    private File pdfGenerado;
    private File docxGenerado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_fiscalizacion_preview);

        tvResumen = findViewById(R.id.tvResumen);
        tvErrores = findViewById(R.id.tvErrores);
        tvEstadoDocs = findViewById(R.id.tvEstadoDocs);
        btnGenerarPdf = findViewById(R.id.btnGenerarPdf);
        btnVerPdf = findViewById(R.id.btnVerPdf);
        btnCompartirPdf = findViewById(R.id.btnCompartirPdf);
        btnAbrirWord = findViewById(R.id.btnAbrirWord);
        panelAcciones = findViewById(R.id.panelAcciones);

        establecimientoId = getIntent().getIntExtra("ESTABLECIMIENTO_ID", -1);
        recuperarDatosIntent();

        mostrarResumen();
        validarDatos();
        panelAcciones.setVisibility(View.GONE);

        btnGenerarPdf.setOnClickListener(v -> generarDocumentos());
        btnVerPdf.setOnClickListener(v -> {
            if (pdfGenerado != null && pdfGenerado.exists()) {
                abrirArchivo(pdfGenerado, "application/pdf");
            }
        });
        btnCompartirPdf.setOnClickListener(v -> compartirPdf());
        btnAbrirWord.setOnClickListener(v -> {
            if (docxGenerado != null && docxGenerado.exists()) {
                abrirArchivo(docxGenerado, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            }
        });
        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
    }

    private void recuperarDatosIntent() {
        fis = new Fiscalizacion();
        fis.establecimientoId = establecimientoId;
        fis.expediente = getIntent().getStringExtra("EXPEDIENTE");
        fis.agenteFiscalizado = getIntent().getStringExtra("AGENTE");
        fis.codigoOsinergmin = getIntent().getStringExtra("CODIGO");
        fis.registroHidrocarburos = getIntent().getStringExtra("REGISTRO");
        fis.fechaDiligencia = getIntent().getStringExtra("FECHA");
        fis.horaApertura = getIntent().getStringExtra("HORA_APE");
        fis.horaCierre = getIntent().getStringExtra("HORA_CIE");
        fis.direccion = getIntent().getStringExtra("DIR");
        fis.distrito = getIntent().getStringExtra("DIST");
        fis.provincia = getIntent().getStringExtra("PROV");
        fis.departamento = getIntent().getStringExtra("DEP");
        fis.rucDni = getIntent().getStringExtra("RUC_DNI");
        fis.telefonoFax = getIntent().getStringExtra("TEL_FAX");
        fis.fiscalizadorResponsable = getIntent().getStringExtra("FISCALIZADOR");
        fis.productosJson = getIntent().getStringExtra("PRODUCTOS_JSON");
        fis.telefonoPublicado = getIntent().getStringExtra("TEL_PUB");
        fis.telefonoActualizado = getIntent().getStringExtra("TEL_ACT");
        fis.horarioPublicado = getIntent().getStringExtra("HOR_PUB");
        fis.listaPreciosExhibida = getIntent().getStringExtra("LISTA_EXP");
        fis.unidadGalonEmpleada = getIntent().getStringExtra("UNIDAD_GAL");
        fis.etiquetaVisible = getIntent().getStringExtra("ETIQUETA_PRE");
        fis.incumplimientosJson = getIntent().getStringExtra("INCUMPLIMIENTOS_JSON");
        fis.hechosVerificados = getIntent().getStringExtra("HECHOS_JSON");
        fis.documentacion = getIntent().getStringExtra("DOCUMENTACION");
        fis.ocurrencias = getIntent().getStringExtra("OCURRENCIAS");

        String manifestaciones = nvl(getIntent().getStringExtra("MANIFESTACIONES"));
        String observaciones = nvl(getIntent().getStringExtra("OBSERVACIONES"));
        if (!manifestaciones.isEmpty() && !observaciones.isEmpty()) {
            fis.observaciones = manifestaciones + "\n" + observaciones;
        } else if (!manifestaciones.isEmpty()) {
            fis.observaciones = manifestaciones;
        } else {
            fis.observaciones = observaciones;
        }

        fis.negativaFirma = getIntent().getBooleanExtra("NEGATIVA_FIRMA", false);
        fis.firmaInspectorPath = getIntent().getStringExtra("PATH_FIRMA_F");
        fis.firmaResponsablePath = getIntent().getStringExtra("PATH_FIRMA_R");

        pathsFirmas.put("firma_f", fis.firmaInspectorPath);
        pathsFirmas.put("firma_r", fis.firmaResponsablePath);
    }

    private void generarDocumentos() {
        btnGenerarPdf.setEnabled(false);
        tvEstadoDocs.setVisibility(View.VISIBLE);
        tvEstadoDocs.setText("Generando Word y PDF con todos los datos...");

        new Thread(() -> {
            try {
                repository.insertFiscalizacion(fis);

                Map<String, String> d = construirMapaDatos();
                List<Map<String, String>> listaProds = construirListaProductos();
                List<Map<String, String>> listaHechos = construirListaHechos();

                WordGenerator.Resultado resultado = WordGenerator.generar(
                        this, TEMPLATE_NAME, d, pathsFirmas, listaProds, listaHechos);

                runOnUiThread(() -> {
                    btnGenerarPdf.setEnabled(true);
                    if (resultado != null && resultado.pdf != null && resultado.pdf.exists()) {
                        docxGenerado = resultado.docx;
                        pdfGenerado = resultado.pdf;
                        panelAcciones.setVisibility(View.VISIBLE);
                        tvEstadoDocs.setText("PDF listo (" + resultado.paginasPdf + " páginas)\n"
                                + pdfGenerado.getName() + "\n"
                                + (docxGenerado != null ? docxGenerado.getName() : ""));
                        Toast.makeText(this,
                                "Acta generada: " + resultado.paginasPdf + " página(s)",
                                Toast.LENGTH_LONG).show();
                        abrirArchivo(pdfGenerado, "application/pdf");
                    } else {
                        tvEstadoDocs.setText("Error al generar. Requiere API + Word en el PC (misma Wi‑Fi).");
                        Toast.makeText(this, "Error: active la API en el PC e intente de nuevo.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnGenerarPdf.setEnabled(true);
                    tvEstadoDocs.setText("Error: " + e.getMessage());
                    Toast.makeText(this, "Error generando documentos", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private Map<String, String> construirMapaDatos() {
        Map<String, String> d = new HashMap<>();
        d.put("expediente", nvl(fis.expediente));
        d.put("agente", nvl(fis.agenteFiscalizado));
        d.put("ruc", nvl(fis.rucDni));
        d.put("codigo", nvl(fis.codigoOsinergmin));
        d.put("registro", nvl(fis.registroHidrocarburos));
        d.put("telefono", nvl(fis.telefonoFax));
        d.put("fecha", nvl(fis.fechaDiligencia));
        d.put("h_inicio", nvl(fis.horaApertura));
        d.put("h_fin", nvl(fis.horaCierre));
        d.put("direccion", nvl(fis.direccion));
        d.put("departamento", nvl(fis.departamento));
        d.put("provincia", nvl(fis.provincia));
        d.put("distrito", nvl(fis.distrito));
        d.put("fiscalizador", nvl(fis.fiscalizadorResponsable));
        d.put("fis_nombres", nvl(fis.fiscalizadorResponsable));
        d.put("fis_dni", "");
        d.put("ocurrencias", nvl(fis.ocurrencias));
        d.put("documentacion", nvl(fis.documentacion));
        d.put("manifestaciones", nvl(fis.observaciones));
        d.put("negativa", fis.negativaFirma ? "SÍ" : "NO");

        d.put("rec_nombres", nvl(getIntent().getStringExtra("RECEPTOR_NOMBRES")));
        d.put("rec_dni", nvl(getIntent().getStringExtra("RECEPTOR_DNI")));
        d.put("rec_relacion", nvl(getIntent().getStringExtra("RECEPTOR_RELACION")));

        marcarSiNo(d, "q1", fis.telefonoPublicado);
        marcarSiNo(d, "q2", fis.telefonoActualizado);
        marcarSiNo(d, "q3", fis.horarioPublicado);
        marcarSiNo(d, "q4", fis.listaPreciosExhibida);
        marcarSiNo(d, "q5", fis.unidadGalonEmpleada);
        marcarSiNo(d, "q6", fis.etiquetaVisible);

        return d;
    }

    private void marcarSiNo(Map<String, String> d, String key, String valor) {
        boolean esSi = valor != null && valor.trim().equalsIgnoreCase("Sí");
        boolean esNo = valor != null && valor.trim().equalsIgnoreCase("No");
        d.put(key, nvl(valor));
        d.put(key + "_si", esSi ? "X" : "");
        d.put(key + "_no", esNo ? "X" : "");
    }

    private List<Map<String, String>> construirListaProductos() {
        List<Map<String, String>> listaProds = new ArrayList<>();
        if (fis.productosJson == null || fis.productosJson.isEmpty()) return listaProds;
        List<ProductoPrecio> prods = new Gson().fromJson(
                fis.productosJson, new TypeToken<List<ProductoPrecio>>(){}.getType());
        if (prods == null) return listaProds;
                for (ProductoPrecio p : prods) {
                    Map<String, String> mapP = new HashMap<>();
                    mapP.put("p_nom", nvl(p.nombre));
                    mapP.put("p_price", nvl(p.precioPrice));
                    mapP.put("p_pub", nvl(p.precioPublicado));
                    mapP.put("p_sur", nvl(p.precioSurtidor));
                    mapP.put("p_desc", nvl(p.precioDescuento));
                    mapP.put("p_marca", nvl(p.marca));
                    listaProds.add(mapP);
                }
        return listaProds;
    }

    private List<Map<String, String>> construirListaHechos() {
        List<Map<String, String>> listaHechos = new ArrayList<>();
        if (fis.hechosVerificados == null || fis.hechosVerificados.isEmpty()) return listaHechos;
        List<HechoVerificado> hechos = new Gson().fromJson(
                fis.hechosVerificados, new TypeToken<List<HechoVerificado>>(){}.getType());
        if (hechos == null) return listaHechos;
        for (HechoVerificado h : hechos) {
            Map<String, String> mapH = new HashMap<>();
            mapH.put("h_inc", nvl(h.incumplimientoNombre));
            mapH.put("h_red", nvl(h.hechoRedactado));
            listaHechos.add(mapH);
        }
        return listaHechos;
    }

    private void mostrarResumen() {
        int nProds = 0;
        int nHechos = 0;
        try {
            if (fis.productosJson != null) {
                List<?> p = new Gson().fromJson(fis.productosJson, new TypeToken<List<ProductoPrecio>>(){}.getType());
                if (p != null) nProds = p.size();
            }
            if (fis.hechosVerificados != null) {
                List<?> h = new Gson().fromJson(fis.hechosVerificados, new TypeToken<List<HechoVerificado>>(){}.getType());
                if (h != null) nHechos = h.size();
            }
        } catch (Exception ignored) {
        }

        tvResumen.setText(
                "Agente: " + nvl(fis.agenteFiscalizado) + "\n" +
                "Expediente: " + nvl(fis.expediente) + "\n" +
                "Código OSINERGMIN: " + nvl(fis.codigoOsinergmin) + "\n" +
                "Registro: " + nvl(fis.registroHidrocarburos) + "\n" +
                "Fecha: " + nvl(fis.fechaDiligencia) + "  " +
                nvl(fis.horaApertura) + " - " + nvl(fis.horaCierre) + "\n" +
                "Dirección: " + nvl(fis.direccion) + "\n" +
                "Ubigeo: " + nvl(fis.distrito) + " / " + nvl(fis.provincia) + " / " + nvl(fis.departamento) + "\n" +
                "RUC/DNI: " + nvl(fis.rucDni) + "   Tel/Fax: " + nvl(fis.telefonoFax) + "\n" +
                "Fiscalizador: " + nvl(fis.fiscalizadorResponsable) + "\n" +
                "Productos: " + nProds + "   Hechos: " + nHechos + "\n" +
                "Verificación: Tel.pub=" + nvl(fis.telefonoPublicado) +
                ", Tel.PRICE=" + nvl(fis.telefonoActualizado) +
                ", Horario=" + nvl(fis.horarioPublicado) + "\n" +
                "Lista=" + nvl(fis.listaPreciosExhibida) +
                ", Galón=" + nvl(fis.unidadGalonEmpleada) +
                ", Etiqueta=" + nvl(fis.etiquetaVisible) + "\n" +
                "Receptor: " + nvl(getIntent().getStringExtra("RECEPTOR_NOMBRES")) +
                " (DNI " + nvl(getIntent().getStringExtra("RECEPTOR_DNI")) + ")\n" +
                "Negativa de firma: " + (fis.negativaFirma ? "SÍ" : "NO")
        );
    }

    private void validarDatos() {
        StringBuilder errores = new StringBuilder();
        if (isBlank(fis.expediente)) errores.append("• Falta expediente\n");
        if (isBlank(fis.agenteFiscalizado)) errores.append("• Falta agente fiscalizado\n");
        if (isBlank(fis.fechaDiligencia)) errores.append("• Falta fecha\n");
        if (isBlank(fis.fiscalizadorResponsable)) errores.append("• Falta fiscalizador\n");
        if (isBlank(fis.firmaInspectorPath)) errores.append("• Falta firma del fiscalizador\n");

        if (errores.length() > 0) {
            tvErrores.setVisibility(View.VISIBLE);
            tvErrores.setText("Revise antes de generar:\n" + errores);
            btnGenerarPdf.setEnabled(false);
        } else {
            tvErrores.setVisibility(View.GONE);
            btnGenerarPdf.setEnabled(true);
        }
    }

    private void compartirPdf() {
        if (pdfGenerado == null || !pdfGenerado.exists()) {
            Toast.makeText(this, "Primero genere el PDF", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", pdfGenerado);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/pdf");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_SUBJECT, "Acta PRICE " + nvl(fis.expediente));
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Compartir acta PDF"));
    }

    private void abrirArchivo(File file, String mimeType) {
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No hay app para abrir este archivo", Toast.LENGTH_LONG).show();
        }
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
