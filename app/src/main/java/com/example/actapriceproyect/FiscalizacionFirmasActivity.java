package com.example.actapriceproyect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FiscalizacionFirmasActivity extends AppCompatActivity {

    private TextInputEditText etDocumentacion, etOcurrencias, etObservaciones;
    private CheckBox cbNegativa;
    private Button btnFinalizar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_fiscalizacion_firmas);

        etDocumentacion = findViewById(R.id.etDocumentacion);
        etOcurrencias = findViewById(R.id.etOcurrencias);
        etObservaciones = findViewById(R.id.etObservaciones);
        cbNegativa = findViewById(R.id.cbNegativaFirma);
        btnFinalizar = findViewById(R.id.btnFinalizar);

        btnFinalizar.setOnClickListener(v -> {
            Intent intent = new Intent(this, FiscalizacionPreviewActivity.class);
            if (getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
            }
            intent.putExtra("DOCUMENTACION", etDocumentacion.getText().toString().trim());
            intent.putExtra("OCURRENCIAS", etOcurrencias.getText().toString().trim());
            intent.putExtra("OBSERVACIONES", etObservaciones.getText().toString().trim());
            intent.putExtra("NEGATIVA_FIRMA", cbNegativa.isChecked());
            startActivity(intent);
        });
    }
}
