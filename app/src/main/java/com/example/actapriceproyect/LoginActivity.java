package com.example.actapriceproyect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.actapriceproyect.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    @Inject
    SessionManager sessionManager;

    private EditText etUsername, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Ocultar la barra de título (banner) programáticamente por si el tema no lo hace
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            // Guardamos un token ficticio y pasamos a la siguiente pantalla
            sessionManager.saveAuthToken("token_sesion_activa");
            Toast.makeText(this, "Sesión Iniciada", Toast.LENGTH_SHORT).show();
            irAEstablecimientos();
        });
    }

    private void irAEstablecimientos() {
        Intent intent = new Intent(this, EstablecimientoActivity.class);
        startActivity(intent);
        finish(); // Cerramos el login para que no se pueda volver atrás con el botón back
    }
}
