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

    private TextInputEditText etNombre, etRuc, etDireccion, etCodigoOsinergmin, 
            etActividad, etNroRegistro, etFechaEmision, etPlacaPrincipal, etUbigeo;
    private Button btnGuardar;
    private int establecimientoId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_add_establecimiento);

        etNombre = findViewById(R.id.etNombre);
        etRuc = findViewById(R.id.etRuc);
        etDireccion = findViewById(R.id.etDireccion);
        etCodigoOsinergmin = findViewById(R.id.etCodigoOsinergmin);
        etActividad = findViewById(R.id.etActividad);
        etNroRegistro = findViewById(R.id.etNroRegistro);
        etFechaEmision = findViewById(R.id.etFechaEmision);
        etPlacaPrincipal = findViewById(R.id.etPlacaPrincipal);
        etUbigeo = findViewById(R.id.etUbigeo);
        btnGuardar = findViewById(R.id.btnGuardar);

        if (getIntent().hasExtra("ID_ESTABLECIMIENTO")) {
            establecimientoId = getIntent().getIntExtra("ID_ESTABLECIMIENTO", -1);
            cargarDatos();
        }

        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void cargarDatos() {
        new Thread(() -> {
            Establecimiento est = repository.getEstablecimientoById(establecimientoId);
            if (est != null) {
                runOnUiThread(() -> {
                    etNombre.setText(est.nombre);
                    etRuc.setText(est.ruc);
                    etDireccion.setText(est.direccion);
                    etCodigoOsinergmin.setText(est.telefono);
                    etActividad.setText(est.actividad);
                    etNroRegistro.setText(est.nroRegistro);
                    etFechaEmision.setText(est.fechaEmision);
                    etPlacaPrincipal.setText(est.placaPrincipal);
                    etUbigeo.setText(est.ubigeo);
                    btnGuardar.setText("ACTUALIZAR ESTABLECIMIENTO");
                });
            }
        }).start();
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String ruc = etRuc.getText().toString().trim();

        if (nombre.isEmpty() || ruc.isEmpty()) {
            Toast.makeText(this, "Nombre y RUC son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        Establecimiento est = new Establecimiento();
        if (establecimientoId != -1) est.id = establecimientoId;
        
        est.nombre = nombre;
        est.ruc = ruc;
        est.direccion = etDireccion.getText().toString().trim();
        est.telefono = etCodigoOsinergmin.getText().toString().trim();
        est.actividad = etActividad.getText().toString().trim();
        est.nroRegistro = etNroRegistro.getText().toString().trim();
        est.fechaEmision = etFechaEmision.getText().toString().trim();
        est.placaPrincipal = etPlacaPrincipal.getText().toString().trim();
        est.ubigeo = etUbigeo.getText().toString().trim();
        est.sincronizado = false;

        new Thread(() -> {
            repository.insertEstablecimiento(est);
            runOnUiThread(() -> {
                Toast.makeText(this, "Establecimiento guardado exitosamente", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
