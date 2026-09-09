package com.example.actapriceproyect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FiscalizacionVerificacionActivity extends AppCompatActivity {

    private RadioGroup rgTelPub, rgTelAct, rgHorPub, rgLista, rgUnidad, rgEtiqueta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_fiscalizacion_verificacion);

        rgTelPub = findViewById(R.id.rgTelefonoPublicado);
        rgTelAct = findViewById(R.id.rgTelefonoActualizado);
        rgHorPub = findViewById(R.id.rgHorarioPublicado);
        rgLista = findViewById(R.id.rgListaPrecios);
        rgUnidad = findViewById(R.id.rgUnidadMedida);
        rgEtiqueta = findViewById(R.id.rgEtiquetaPrecio);

        findViewById(R.id.btnSiguiente).setOnClickListener(v -> {
            if (rgTelPub.getCheckedRadioButtonId() == -1 || 
                rgTelAct.getCheckedRadioButtonId() == -1 || 
                rgHorPub.getCheckedRadioButtonId() == -1 ||
                rgLista.getCheckedRadioButtonId() == -1 ||
                rgUnidad.getCheckedRadioButtonId() == -1 ||
                rgEtiqueta.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Debe responder todas las preguntas de verificación", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, FiscalizacionIncumplimientosActivity.class);
            if (getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
            }
            
            intent.putExtra("TEL_PUB", rgTelPub.getCheckedRadioButtonId() == R.id.rbTelPubSi ? "Sí" : "No");
            intent.putExtra("TEL_ACT", rgTelAct.getCheckedRadioButtonId() == R.id.rbTelActSi ? "Sí" : "No");
            intent.putExtra("HOR_PUB", rgHorPub.getCheckedRadioButtonId() == R.id.rbHorPubSi ? "Sí" : "No");
            intent.putExtra("LISTA_EXP", rgLista.getCheckedRadioButtonId() == R.id.rbListaSi ? "Sí" : "No");
            intent.putExtra("UNIDAD_GAL", rgUnidad.getCheckedRadioButtonId() == R.id.rbUnidadSi ? "Sí" : "No");
            intent.putExtra("ETIQUETA_PRE", rgEtiqueta.getCheckedRadioButtonId() == R.id.rbEtiquetaSi ? "Sí" : "No");

            startActivity(intent);
        });
    }
}
