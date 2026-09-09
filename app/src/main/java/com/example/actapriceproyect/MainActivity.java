package com.example.actapriceproyect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.actapriceproyect.repository.ActaRepository;
import com.example.actapriceproyect.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    ActaRepository repository;

    @Inject
    SessionManager sessionManager;

    private int establecimientoId;
    private String establecimientoNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ocultar barra de título
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main);

        establecimientoId = getIntent().getIntExtra("ID_ESTABLECIMIENTO", -1);
        establecimientoNombre = getIntent().getStringExtra("NOMBRE_ESTABLECIMIENTO");

        if (establecimientoId == -1) {
            startActivity(new Intent(this, EstablecimientoActivity.class));
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Gestión: " + establecimientoNombre);

        findViewById(R.id.btnListarEstablecimientos).setOnClickListener(v -> {
            startActivity(new Intent(this, EstablecimientoActivity.class));
            finish();
        });

        findViewById(R.id.btnNuevaFiscalizacion).setOnClickListener(v -> {
            Intent intent = new Intent(this, FiscalizacionGeneralActivity.class);
            intent.putExtra("ESTABLECIMIENTO_ID", establecimientoId);
            startActivity(intent);
        });

        findViewById(R.id.btnSincronizar).setOnClickListener(v -> {
            repository.syncEstablecimientos();
            repository.syncFiscalizaciones();
            Toast.makeText(this, "Sincronización iniciada", Toast.LENGTH_SHORT).show();
        });
    }
}
