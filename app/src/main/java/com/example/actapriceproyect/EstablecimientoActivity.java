package com.example.actapriceproyect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actapriceproyect.model.Establecimiento;
import com.example.actapriceproyect.repository.ActaRepository;
import com.example.actapriceproyect.ui.EstablecimientoAdapter;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EstablecimientoActivity extends AppCompatActivity {

    @Inject
    ActaRepository repository;

    private RecyclerView rvEstablecimientos;
    private EstablecimientoAdapter adapter;
    private TextView tvEmptyState;
    private SearchView svBuscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_establecimiento_list);

        tvEmptyState = findViewById(R.id.tvEmptyState);
        svBuscar = findViewById(R.id.svBuscar);
        rvEstablecimientos = findViewById(R.id.rvEstablecimientos);
        
        rvEstablecimientos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EstablecimientoAdapter();
        rvEstablecimientos.setAdapter(adapter);

        // Configuración del buscador (Módulo 03 - Búsqueda)
        svBuscar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });

        // Al seleccionar un establecimiento, vamos al Menú Principal (Módulo 02)
        adapter.setOnItemClickListener(est -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("ID_ESTABLECIMIENTO", est.id);
            intent.putExtra("NOMBRE_ESTABLECIMIENTO", est.nombre);
            startActivity(intent);
        });

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            startActivity(new Intent(this, AddEstablecimientoActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatos();
    }

    private void cargarDatos() {
        new Thread(() -> {
            List<Establecimiento> lista = repository.getAllEstablecimientos();
            runOnUiThread(() -> {
                adapter.setEstablecimientos(lista);
                if (lista.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvEstablecimientos.setVisibility(View.GONE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    rvEstablecimientos.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }
}
