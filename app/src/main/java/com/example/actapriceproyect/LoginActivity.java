package com.example.actapriceproyect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.actapriceproyect.model.LoginRequest;
import com.example.actapriceproyect.model.LoginResponse;
import com.example.actapriceproyect.network.ApiService;
import com.example.actapriceproyect.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    @Inject
    SessionManager sessionManager;

    @Inject
    ApiService apiService;

    private EditText etUsername, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Valores demo
        if (etUsername.getText() == null || etUsername.getText().toString().isEmpty()) {
            etUsername.setText("fiscalizador");
        }
        if (etPassword.getText() == null || etPassword.getText().toString().isEmpty()) {
            etPassword.setText("price2026");
        }

        btnLogin.setOnClickListener(v -> intentarLogin());
    }

    private void intentarLogin() {
        String user = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String pass = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        apiService.login(new LoginRequest(user, pass)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().token != null) {
                    LoginResponse body = response.body();
                    sessionManager.saveAuthToken(body.token);
                    if (body.nombre != null) sessionManager.saveUserName(body.nombre);
                    if (body.rol != null) sessionManager.saveUserRole(body.rol);
                    Toast.makeText(LoginActivity.this, "Sesión iniciada", Toast.LENGTH_SHORT).show();
                    irAEstablecimientos();
                } else {
                    Toast.makeText(LoginActivity.this, "Credenciales inválidas", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                // Fallback offline para demos sin API
                sessionManager.saveAuthToken("offline-demo-token");
                sessionManager.saveUserName(user);
                Toast.makeText(LoginActivity.this,
                        "API no disponible. Entrando en modo offline.",
                        Toast.LENGTH_LONG).show();
                irAEstablecimientos();
            }
        });
    }

    private void irAEstablecimientos() {
        Intent intent = new Intent(this, EstablecimientoActivity.class);
        startActivity(intent);
        finish();
    }
}
