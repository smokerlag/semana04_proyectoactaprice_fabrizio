package com.example.actapriceproyect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actapriceproyect.model.ProductoPrecio;
import com.example.actapriceproyect.ui.PrecioAdapter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FiscalizacionPreciosActivity extends AppCompatActivity {

    private RecyclerView rvPrecios;
    private PrecioAdapter adapter;
    private List<ProductoPrecio> listaProductos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_fiscalizacion_precios);

        rvPrecios = findViewById(R.id.rvPrecios);
        rvPrecios.setLayoutManager(new LinearLayoutManager(this));

        inicializarProductos();
        adapter = new PrecioAdapter(listaProductos);
        rvPrecios.setAdapter(adapter);

        findViewById(R.id.btnSiguiente).setOnClickListener(v -> {
            if (validarPrecios()) {
                // Cada campo de precio se guarda tal cual (pueden ser distintos entre sí)
                String productosJson = new Gson().toJson(listaProductos);

                Intent intent = new Intent(this, FiscalizacionVerificacionActivity.class);
                if (getIntent().getExtras() != null) {
                    intent.putExtras(getIntent().getExtras());
                }
                intent.putExtra("PRODUCTOS_JSON", productosJson);
                startActivity(intent);
            }
        });
    }

    private boolean validarPrecios() {
        for (ProductoPrecio p : listaProductos) {
            // Solo se valida formato; no se exige que PRICE = publicado = surtidor = descuento
            if (isNegative(p.precioPrice) || isNegative(p.precioPublicado) ||
                isNegative(p.precioSurtidor) || isNegative(p.precioDescuento)) {
                Toast.makeText(this, "No se permiten precios negativos en " + p.nombre, Toast.LENGTH_SHORT).show();
                return false;
            }
            if (!isValidNumberOrEmpty(p.precioPrice) || !isValidNumberOrEmpty(p.precioPublicado) ||
                !isValidNumberOrEmpty(p.precioSurtidor) || !isValidNumberOrEmpty(p.precioDescuento)) {
                Toast.makeText(this, "Precio inválido en " + p.nombre, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private boolean isValidNumberOrEmpty(String val) {
        if (val == null || val.trim().isEmpty()) return true;
        try {
            Double.parseDouble(val.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isNegative(String val) {
        if (val == null || val.trim().isEmpty()) return false;
        try {
            double d = Double.parseDouble(val);
            return d < 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void inicializarProductos() {
        listaProductos = new ArrayList<>();
        // Orden alineado a la plantilla del acta
        listaProductos.add(new ProductoPrecio("Diesel B5 S-50"));
        listaProductos.add(new ProductoPrecio("Gasohol 84 Plus"));
        listaProductos.add(new ProductoPrecio("Gasohol Regular"));
        listaProductos.add(new ProductoPrecio("Gasohol Premium"));
        listaProductos.add(new ProductoPrecio("GLP Automotor"));
        // En el acta oficial hay 2 columnas: OTROS (precios) y MARCA (nombre comercial)
        listaProductos.add(new ProductoPrecio("Otros", true));
        listaProductos.add(new ProductoPrecio("GLP Envasado 3 kg"));
        listaProductos.add(new ProductoPrecio("GLP Envasado 5 kg"));
        listaProductos.add(new ProductoPrecio("GLP Envasado 10 kg"));
        listaProductos.add(new ProductoPrecio("GLP Envasado 15 kg"));
        listaProductos.add(new ProductoPrecio("GLP Envasado 45 kg"));
    }
}
