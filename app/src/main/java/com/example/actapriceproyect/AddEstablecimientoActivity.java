package com.example.actapriceproyect;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.actapriceproyect.model.Establecimiento;
import com.example.actapriceproyect.repository.ActaRepository;
import com.google.android.material.textfield.TextInputEditText;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddEstablecimientoActivity extends AppCompatActivity {

    @Inject
    ActaRepository repository;

    private TextInputEditText etNombre, etRuc, etDireccion, etTelefono;
    private Button btnGuardar;
    private int establecimientoId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Ocultar la barra de título (banner)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_add_establecimiento);

        etNombre = findViewById(R.id.etNombre);
        etRuc = findViewById(R.id.etRuc);
        etDireccion = findViewById(R.id.etDireccion);
        etTelefono = findViewById(R.id.etTelefono);
        btnGuardar = findViewById(R.id.btnGuardar);

        if (getIntent().hasExtra("ID_ESTABLECIMIENTO")) {
            establecimientoId = getIntent().getIntExtra("ID_ESTABLECIMIENTO", -1);
            cargarDatos();
        }

        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void cargarDatos() {
        new Thread(() -> {
            Establecimiento est = repository.getAllEstablecimientos().stream()
                    .filter(e -> e.id == establecimientoId)
                    .findFirst()
                    .orElse(null);
            if (est != null) {
                runOnUiThread(() -> {
                    etNombre.setText(est.nombre);
                    etRuc.setText(est.ruc);
                    etDireccion.setText(est.direccion);
                    etTelefono.setText(est.telefono);
                    btnGuardar.setText("Actualizar Establecimiento");
                });
            }
        }).start();
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String ruc = etRuc.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if (nombre.isEmpty() || ruc.isEmpty()) {
            Toast.makeText(this, "Nombre y RUC son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        Establecimiento est = new Establecimiento();
        if (establecimientoId != -1) est.id = establecimientoId;
        est.nombre = nombre;
        est.ruc = ruc;
        est.direccion = direccion;
        est.telefono = telefono;
        est.sincronizado = false;

        new Thread(() -> {
            repository.insertEstablecimiento(est);
            runOnUiThread(() -> {
                Toast.makeText(this, "Establecimiento guardado", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
