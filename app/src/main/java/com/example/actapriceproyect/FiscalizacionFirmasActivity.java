package com.example.actapriceproyect;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.actapriceproyect.ui.SignatureView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FiscalizacionFirmasActivity extends AppCompatActivity {

    private TextInputEditText etOcurrencias, etDocumentacion, etManifestaciones, 
            etReceptorNombres, etReceptorDni, etReceptorRelacion;
    private SignatureView signatureF, signatureR;
    private CheckBox cbNegativaCompleta;
    private Button btnFinalizar, btnClearF, btnClearR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_fiscalizacion_firmas);

        etOcurrencias = findViewById(R.id.etOcurrencias);
        etDocumentacion = findViewById(R.id.etDocumentacion);
        etManifestaciones = findViewById(R.id.etManifestaciones);
        etReceptorNombres = findViewById(R.id.etReceptorNombresApellidos);
        etReceptorDni = findViewById(R.id.etReceptorDni);
        etReceptorRelacion = findViewById(R.id.etReceptorRelacion);
        signatureF = findViewById(R.id.signatureFiscalizador);
        signatureR = findViewById(R.id.signatureReceptor);
        cbNegativaCompleta = findViewById(R.id.cbNegativaCompleta);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        btnClearF = findViewById(R.id.btnClearFirmaF);
        btnClearR = findViewById(R.id.btnClearFirmaR);

        btnClearF.setOnClickListener(v -> signatureF.clear());
        btnClearR.setOnClickListener(v -> signatureR.clear());

        cbNegativaCompleta.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etReceptorNombres.setText("");
                etReceptorDni.setText("");
                etReceptorRelacion.setText("");
                etReceptorNombres.setEnabled(false);
                etReceptorDni.setEnabled(false);
                etReceptorRelacion.setEnabled(false);
                signatureR.clear();
                signatureR.setEnabled(false);
            } else {
                etReceptorNombres.setEnabled(true);
                etReceptorDni.setEnabled(true);
                etReceptorRelacion.setEnabled(true);
                signatureR.setEnabled(true);
            }
        });

        btnFinalizar.setOnClickListener(v -> {
            if (validarCampos()) {
                String pathF = guardarFirmaTemporal(signatureF.getSignatureBitmap(), "firma_f.png");
                String pathR = !cbNegativaCompleta.isChecked() ? guardarFirmaTemporal(signatureR.getSignatureBitmap(), "firma_r.png") : null;

                Intent intent = new Intent(this, FiscalizacionPreviewActivity.class);
                if (getIntent().getExtras() != null) {
                    intent.putExtras(getIntent().getExtras());
                }
                intent.putExtra("OCURRENCIAS", etOcurrencias.getText().toString().trim());
                intent.putExtra("DOCUMENTACION", etDocumentacion.getText().toString().trim());
                intent.putExtra("MANIFESTACIONES", etManifestaciones.getText().toString().trim());
                intent.putExtra("NEGATIVA_FIRMA", cbNegativaCompleta.isChecked());
                intent.putExtra("RECEPTOR_NOMBRES", etReceptorNombres.getText().toString().trim());
                intent.putExtra("RECEPTOR_DNI", etReceptorDni.getText().toString().trim());
                intent.putExtra("RECEPTOR_RELACION", etReceptorRelacion.getText().toString().trim());
                intent.putExtra("PATH_FIRMA_F", pathF);
                intent.putExtra("PATH_FIRMA_R", pathR);
                startActivity(intent);
            }
        });
    }

    private String guardarFirmaTemporal(Bitmap bitmap, String filename) {
        if (bitmap == null) return null;
        try {
            File file = new File(getCacheDir(), filename);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean validarCampos() {
        if (signatureF.isCanvasEmpty()) {
            Toast.makeText(this, "La firma del fiscalizador es obligatoria", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!cbNegativaCompleta.isChecked()) {
            if (signatureR.isCanvasEmpty()) {
                Toast.makeText(this, "La firma del receptor es obligatoria (o marque negativa)", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (etReceptorNombres.getText().toString().trim().isEmpty() ||
                etReceptorDni.getText().toString().trim().isEmpty() ||
                etReceptorRelacion.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Complete los datos del receptor", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
}
