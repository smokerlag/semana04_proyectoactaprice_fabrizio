package com.example.actapriceproyect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actapriceproyect.model.ProductoPrecio;
import com.example.actapriceproyect.ui.IncumplimientoAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FiscalizacionIncumplimientosActivity extends AppCompatActivity {

    private RecyclerView rvIncumplimientos;
    private IncumplimientoAdapter adapter;
    private List<String> incumplimientosDetectados;
    private TextView tvEmpty;
    private Button btnSiguiente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_fiscalizacion_incumplimientos);

        rvIncumplimientos = findViewById(R.id.rvIncumplimientosDetectados);
        tvEmpty = findViewById(R.id.tvEmptyIncumplimientos);
        btnSiguiente = findViewById(R.id.btnSiguiente);

        rvIncumplimientos.setLayoutManager(new LinearLayoutManager(this));

        detectarIncumplimientos();

        // Configuración del adaptador con el listener para el botón "CORREGIR"
        adapter = new IncumplimientoAdapter(incumplimientosDetectados, incumplimiento -> {
            Intent intent;
            if (incumplimiento.contains("precios") || incumplimiento.contains("ALERTA")) {
                // Redirigir a la pantalla de Precios
                intent = new Intent(this, FiscalizacionPreciosActivity.class);
            } else if (incumplimiento.contains("Incumplimiento 2") || 
                       incumplimiento.contains("Incumplimiento 3") || 
                       incumplimiento.contains("Incumplimiento 4") || 
                       incumplimiento.contains("Incumplimiento 5") || 
                       incumplimiento.contains("Incumplimiento 6")) {
                // Redirigir a la pantalla de Verificación
                intent = new Intent(this, FiscalizacionVerificacionActivity.class);
            } else {
                // Por defecto a Datos Generales
                intent = new Intent(this, FiscalizacionGeneralActivity.class);
            }
            
            // Pasar los extras actuales para mantener la información ya ingresada
            if (getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
            }
            
            // Usar flags para volver a la instancia existente de la actividad y limpiar las superiores
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        
        rvIncumplimientos.setAdapter(adapter);

        actualizarEstadoUI();

        btnSiguiente.setOnClickListener(v -> {
            JSONArray jsonArray = new JSONArray();
            for (String s : incumplimientosDetectados) {
                jsonArray.put(s);
            }

            Intent intent = new Intent(this, FiscalizacionHechosActivity.class);
            if (getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
            }
            intent.putExtra("INCUMPLIMIENTOS_JSON", jsonArray.toString());
            startActivity(intent);
        });
    }

    private void actualizarEstadoUI() {
        if (incumplimientosDetectados.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("SITUACIÓN CONFORME:\nNo se detectaron discrepancias ni omisiones. Puede continuar.");
            rvIncumplimientos.setVisibility(View.GONE);
            btnSiguiente.setText("CONTINUAR A HECHOS VERIFICADOS");
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvIncumplimientos.setVisibility(View.VISIBLE);
            btnSiguiente.setText("IGNORAR Y CONTINUAR");
        }
    }

    private void detectarIncumplimientos() {
        incumplimientosDetectados = new ArrayList<>();

        // --- 1. ANÁLISIS AUTOMÁTICO DE PRECIOS ---
        String productosJson = getIntent().getStringExtra("PRODUCTOS_JSON");
        if (productosJson != null) {
            List<ProductoPrecio> productos = new Gson().fromJson(productosJson, new TypeToken<List<ProductoPrecio>>(){}.getType());
            boolean inc1Detectado = false;
            boolean algunPrecioIngresado = false;

            for (ProductoPrecio p : productos) {
                String prPrice = (p.precioPrice != null) ? p.precioPrice.trim() : "";
                String prPub = (p.precioPublicado != null) ? p.precioPublicado.trim() : "";
                String prSur = (p.precioSurtidor != null) ? p.precioSurtidor.trim() : "";

                if (!prPrice.isEmpty() || !prPub.isEmpty() || !prSur.isEmpty()) algunPrecioIngresado = true;

                if ((!prPub.isEmpty() || !prSur.isEmpty()) && prPrice.isEmpty()) inc1Detectado = true;
                if (!prPrice.isEmpty() && !prPub.isEmpty() && !prPrice.equals(prPub)) inc1Detectado = true;
                if (!prPrice.isEmpty() && !prSur.isEmpty() && !prPrice.equals(prSur)) inc1Detectado = true;
            }

            if (inc1Detectado) {
                incumplimientosDetectados.add("Incumplimiento 1: discrepancia detectada o falta de precios en PRICE.");
            }
            if (!algunPrecioIngresado) {
                incumplimientosDetectados.add("ALERTA: No se ha registrado información de precios.");
            }
        }

        // --- 2. ANÁLISIS AUTOMÁTICO DE VERIFICACIÓN ---
        if ("No".equals(getIntent().getStringExtra("LISTA_EXP"))) 
            incumplimientosDetectados.add("Incumplimiento 2: no exhibición de la lista de precios vigente.");
        if ("No".equals(getIntent().getStringExtra("TEL_ACT"))) 
            incumplimientosDetectados.add("Incumplimiento 3: ubicación o teléfono no actualizado en PRICE.");
        if ("No".equals(getIntent().getStringExtra("TEL_PUB")) || "No".equals(getIntent().getStringExtra("HOR_PUB"))) 
            incumplimientosDetectados.add("Incumplimiento 4: horario o teléfono no exhibidos en local.");
        if ("No".equals(getIntent().getStringExtra("UNIDAD_GAL"))) 
            incumplimientosDetectados.add("Incumplimiento 5: no uso del galón como unidad.");
        if ("No".equals(getIntent().getStringExtra("ETIQUETA_PRE"))) 
            incumplimientosDetectados.add("Incumplimiento 6: falta de etiqueta visible de precio.");
    }
}
