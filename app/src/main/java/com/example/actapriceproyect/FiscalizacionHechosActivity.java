package com.example.actapriceproyect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actapriceproyect.model.HechoVerificado;
import com.example.actapriceproyect.ui.HechosAdapter;
import com.example.actapriceproyect.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FiscalizacionHechosActivity extends AppCompatActivity {

    @Inject
    SessionManager sessionManager;

    private RecyclerView rvHechos;
    private HechosAdapter adapter;
    private List<HechoVerificado> listaHechos;
    private TextInputEditText etOcurrencias, etObservaciones;
    private Button btnSiguiente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_fiscalizacion_hechos);

        rvHechos = findViewById(R.id.rvHechos);
        etOcurrencias = findViewById(R.id.etOcurrencias);
        etObservaciones = findViewById(R.id.etObservaciones);
        btnSiguiente = findViewById(R.id.btnSiguiente);

        rvHechos.setLayoutManager(new LinearLayoutManager(this));

        prepararListaHechos();
        if (listaHechos.isEmpty()) {
            // Sin incumplimientos 1-6: ir directo a firmas
            Intent intent = new Intent(this, FiscalizacionFirmasActivity.class);
            if (getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
            }
            intent.putExtra("HECHOS_JSON", "[]");
            startActivity(intent);
            finish();
            return;
        }

        adapter = new HechosAdapter(listaHechos);
        rvHechos.setAdapter(adapter);

        btnSiguiente.setOnClickListener(v -> {
            if (validarHechos()) {
                String hechosJson = new Gson().toJson(listaHechos);
                
                Intent intent = new Intent(this, FiscalizacionFirmasActivity.class);
                if (getIntent().getExtras() != null) {
                    intent.putExtras(getIntent().getExtras());
                }
                intent.putExtra("HECHOS_JSON", hechosJson);
                intent.putExtra("OCURRENCIAS", etOcurrencias.getText().toString().trim());
                intent.putExtra("OBSERVACIONES", etObservaciones.getText().toString().trim());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Debe redactar los hechos para cada incumplimiento seleccionado", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void prepararListaHechos() {
        listaHechos = new ArrayList<>();
        String incumplimientosJson = getIntent().getStringExtra("INCUMPLIMIENTOS_JSON");
        String fechaActual = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        String usuarioActual = sessionManager.getUserName();

        if (incumplimientosJson != null) {
            try {
                JSONArray jsonArray = new JSONArray(incumplimientosJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String nombre = jsonArray.getString(i);
                    // Solo incumplimientos 1–6 del acta oficial
                    if (nombre != null && nombre.matches("(?i).*Incumplimiento\\s*[1-6].*")) {
                        listaHechos.add(new HechoVerificado(nombre, fechaActual, usuarioActual));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validarHechos() {
        for (HechoVerificado h : listaHechos) {
            if (h.hechoRedactado.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
