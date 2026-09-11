package com.example.actapriceproyect;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.actapriceproyect.repository.ActaRepository;
import com.google.android.material.textfield.TextInputEditText;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FiscalizacionGeneralActivity extends AppCompatActivity {
    @Inject ActaRepository repository;
    private TextInputEditText etExpediente, etAgente, etCodigo, etRegistro, etFecha, 
            etHoraApertura, etHoraCierre, etDireccion, etDistrito, etProvincia, 
            etDepartamento, etRucDni, etTelefonoFax, etFiscalizador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_fiscalizacion_general);

        etExpediente = findViewById(R.id.etExpediente);
        etAgente = findViewById(R.id.etAgenteFiscalizado);
        etCodigo = findViewById(R.id.etCodigoOsinergmin);
        etRegistro = findViewById(R.id.etRegistroHidrocarburos);
        etFecha = findViewById(R.id.etFechaDiligencia);
        etHoraApertura = findViewById(R.id.etHoraApertura);
        etHoraCierre = findViewById(R.id.etHoraCierre);
        etDireccion = findViewById(R.id.etDireccion);
        etDistrito = findViewById(R.id.etDistrito);
        etProvincia = findViewById(R.id.etProvincia);
        etDepartamento = findViewById(R.id.etDepartamento);
        etRucDni = findViewById(R.id.etRucDni);
        etTelefonoFax = findViewById(R.id.etTelefonoFax);
        etFiscalizador = findViewById(R.id.etFiscalizadorResponsable);

        // Carga y Precarga inteligente desde Base de Datos Local
        int establecimientoId = getIntent().getIntExtra("ESTABLECIMIENTO_ID", -1);
        if (establecimientoId != -1) {
            new Thread(() -> {
                com.example.actapriceproyect.model.Establecimiento est = repository.getEstablecimientoById(establecimientoId);
                if (est != null) {
                    runOnUiThread(() -> {
                        etAgente.setText(est.nombre);
                        etRucDni.setText(est.ruc);
                        etDireccion.setText(est.direccion);
                        etCodigo.setText(est.telefono); // Código Osinergmin mapeado

                        // Procesar UBIGEO (Formato esperado: DEPARTAMENTO / PROVINCIA / DISTRITO)
                        if (est.ubigeo != null && est.ubigeo.contains("/")) {
                            String[] partes = est.ubigeo.split("/");
                            if (partes.length >= 1) etDepartamento.setText(partes[0].trim());
                            if (partes.length >= 2) etProvincia.setText(partes[1].trim());
                            if (partes.length >= 3) etDistrito.setText(partes[2].trim());
                        }

                        // Bloquear campos para evitar modificaciones accidentales
                        etAgente.setFocusable(false);
                        etAgente.setClickable(false);
                        etRucDni.setFocusable(false);
                        etRucDni.setClickable(false);
                        etDireccion.setFocusable(false);
                        etDireccion.setClickable(false);
                        etCodigo.setFocusable(false);
                        etCodigo.setClickable(false);
                        etDepartamento.setFocusable(false);
                        etDepartamento.setClickable(false);
                        etProvincia.setFocusable(false);
                        etProvincia.setClickable(false);
                        etDistrito.setFocusable(false);
                        etDistrito.setClickable(false);
                    });
                }
            }).start();
        }

        // Configurar Date y Time Pickers
        etFecha.setOnClickListener(v -> showDatePickerDialog(etFecha));
        etHoraApertura.setOnClickListener(v -> showTimePickerDialog(etHoraApertura));
        etHoraCierre.setOnClickListener(v -> showTimePickerDialog(etHoraCierre));

        findViewById(R.id.btnSiguiente).setOnClickListener(v -> {
            if (validarCampos()) {
                Intent intent = new Intent(this, FiscalizacionPreciosActivity.class);
                if (getIntent().getExtras() != null) {
                    intent.putExtras(getIntent().getExtras());
                }
                intent.putExtra("EXPEDIENTE", etExpediente.getText().toString());
                intent.putExtra("AGENTE", etAgente.getText().toString());
                intent.putExtra("CODIGO", etCodigo.getText().toString());
                intent.putExtra("REGISTRO", etRegistro.getText().toString());
                intent.putExtra("FECHA", etFecha.getText().toString());
                intent.putExtra("HORA_APE", etHoraApertura.getText().toString());
                intent.putExtra("HORA_CIE", etHoraCierre.getText().toString());
                intent.putExtra("DIR", etDireccion.getText().toString());
                intent.putExtra("DIST", etDistrito.getText().toString());
                intent.putExtra("PROV", etProvincia.getText().toString());
                intent.putExtra("DEP", etDepartamento.getText().toString());
                intent.putExtra("RUC_DNI", etRucDni.getText().toString());
                intent.putExtra("TEL_FAX", etTelefonoFax.getText().toString());
                intent.putExtra("FISCALIZADOR", etFiscalizador.getText().toString());
                startActivity(intent);
            }
        });
    }

    private void showDatePickerDialog(TextInputEditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year1);
                    editText.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog(TextInputEditText editText) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                    editText.setText(selectedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private boolean validarCampos() {
        if (isEmpty(etExpediente) || isEmpty(etAgente) || isEmpty(etCodigo) || 
            isEmpty(etRucDni) || isEmpty(etFecha) || isEmpty(etHoraApertura) || 
            isEmpty(etHoraCierre) || isEmpty(etFiscalizador)) {
            Toast.makeText(this, "Todos los campos obligatorios deben estar llenos", Toast.LENGTH_LONG).show();
            return false;
        }

        // Validación: Cierre posterior a apertura
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date apertura = sdf.parse(etHoraApertura.getText().toString());
            Date cierre = sdf.parse(etHoraCierre.getText().toString());
            if (cierre != null && apertura != null && cierre.before(apertura)) {
                Toast.makeText(this, "La hora de cierre no puede ser anterior a la de apertura", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Formato de hora inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isEmpty(TextInputEditText et) {
        return et.getText().toString().trim().isEmpty();
    }
}
