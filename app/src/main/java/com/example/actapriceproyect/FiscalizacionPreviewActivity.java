package com.example.actapriceproyect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.actapriceproyect.model.Establecimiento;
import com.example.actapriceproyect.model.Fiscalizacion;
import com.example.actapriceproyect.model.HechoVerificado;
import com.example.actapriceproyect.model.ProductoPrecio;
import com.example.actapriceproyect.repository.ActaRepository;
import com.example.actapriceproyect.utils.PdfGenerator;
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

    @Inject
    ActaRepository repository;

    private TextView tvResumen, tvErrores;
    private Button btnGenerarPdf;
    private Fiscalizacion fis;
    private int establecimientoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_fiscalizacion_preview);

        tvResumen = findViewById(R.id.tvResumen);
        tvErrores = findViewById(R.id.tvErrores);
        btnGenerarPdf = findViewById(R.id.btnGenerarPdf);

        establecimientoId = getIntent().getIntExtra("ESTABLECIMIENTO_ID", -1);
        recuperarDatosIntent();

        mostrarResumen();
        validarDatos();

        btnGenerarPdf.setOnClickListener(v -> generarDocumentos());
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
        fis.incumplimientosJson = getIntent().getStringExtra("INCUMPLIMIENTOS_JSON");
        fis.hechosVerificados = getIntent().getStringExtra("HECHOS_JSON");
        fis.documentacion = getIntent().getStringExtra("DOCUMENTACION");
        fis.ocurrencias = getIntent().getStringExtra("OCURRENCIAS");
        fis.observaciones = getIntent().getStringExtra("OBSERVACIONES");
        fis.negativaFirma = getIntent().getBooleanExtra("NEGATIVA_FIRMA", false);
    }

    private void generarDocumentos() {
        new Thread(() -> {
            repository.insertFiscalizacion(fis);
            
            // 1. Preparar Mapa de Datos para el Word
            Map<String, String> d = new HashMap<>();
            d.put("expediente", fis.expediente);
            d.put("agente", fis.agenteFiscalizado);
            d.put("ruc", fis.rucDni);
            d.put("fecha", fis.fechaDiligencia);
            d.put("h_inicio", fis.horaApertura);
            d.put("h_fin", fis.horaCierre);
            d.put("direccion", fis.direccion);
            d.put("fiscalizador", fis.fiscalizadorResponsable);
            
            // Mapeo dinámico de productos para el Word
            if (fis.productosJson != null) {
                List<ProductoPrecio> prods = new Gson().fromJson(fis.productosJson, new TypeToken<List<ProductoPrecio>>(){}.getType());
                for (int i = 0; i < prods.size(); i++) {
                    ProductoPrecio p = prods.get(i);
                    d.put("p" + i + "_nom", p.nombre);
                    d.put("p" + i + "_price", p.precioPrice);
                    d.put("p" + i + "_pub", p.precioPublicado);
                    d.put("p" + i + "_sur", p.precioSurtidor);
                    d.put("p" + i + "_desc", p.precioDescuento);
                }
            }

            // Mapeo de Hechos
            StringBuilder hechosTxt = new StringBuilder();
            if (fis.hechosVerificados != null) {
                List<HechoVerificado> hechos = new Gson().fromJson(fis.hechosVerificados, new TypeToken<List<HechoVerificado>>(){}.getType());
                for (HechoVerificado h : hechos) {
                    hechosTxt.append("• ").append(h.incumplimientoNombre).append(": ").append(h.hechoRedactado).append("\n");
                }
            }
            d.put("hechos", hechosTxt.toString());

            // 2. Generar el Word (debe estar en assets/plantilla_acta.docx)
            File wordFile = WordGenerator.generarWordDesdePlantilla(this, "plantilla_acta.docx", "Acta_PRICE_" + fis.id + ".docx", d);

            // 3. Generar el PDF para visualización
            Establecimiento est = repository.getAllEstablecimientos().stream().filter(e -> e.id == establecimientoId).findFirst().orElse(new Establecimiento());
            File pdfFile = PdfGenerator.generarActaPrice(this, est, fis);
            
            runOnUiThread(() -> {
                if (pdfFile != null) {
                    Toast.makeText(this, "Acta generada (Word y PDF)", Toast.LENGTH_LONG).show();
                    abrirPdf(pdfFile);
                } else {
                    Toast.makeText(this, "Error: Suba 'plantilla_acta.docx' a la carpeta assets.", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void mostrarResumen() {
        tvResumen.setText("Resumen listo para generar Acta de: " + fis.agenteFiscalizado + "\nExpediente: " + fis.expediente);
    }

    private void validarDatos() {
        if (fis.expediente == null || fis.expediente.isEmpty()) {
            tvErrores.setVisibility(View.VISIBLE);
            tvErrores.setText("ERROR: El N° de Expediente es obligatorio.");
            btnGenerarPdf.setEnabled(false);
        } else {
            tvErrores.setVisibility(View.GONE);
            btnGenerarPdf.setEnabled(true);
        }
    }

    private void abrirPdf(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}
