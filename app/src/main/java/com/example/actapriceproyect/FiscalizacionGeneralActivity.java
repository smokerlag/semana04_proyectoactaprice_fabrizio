package com.example.actapriceproyect;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.actapriceproyect.repository.ActaRepository;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
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
        if (isEmpty(etExpediente) || isEmpty(etAgente) || isEmpty(etCodigo) || isEmpty(etRucDni) || 
            isEmpty(etFecha) || isEmpty(etHoraApertura)) {
            Toast.makeText(this, "Debe completar los datos obligatorios (incluyendo Fecha y Hora)", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean isEmpty(TextInputEditText et) {
        return et.getText().toString().trim().isEmpty();
    }
}
