package com.example.actapriceproyect.utils;

import android.content.Context;
import android.util.Log;
import com.example.actapriceproyect.model.Establecimiento;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvUtil {

    public static List<Establecimiento> cargarEstablecimientosDesdeCsv(Context context) {
        List<Establecimiento> lista = new ArrayList<>();
        Log.d("CsvUtil", "Iniciando lectura de CSV expandido...");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("establecimientos.csv"), StandardCharsets.UTF_8))) {
            
            String line;
            boolean headerFound = false;
            
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                
                // Ignorar metadatos iniciales hasta encontrar la cabecera real
                if (!headerFound) {
                    if (trimmed.startsWith("N°;") || trimmed.contains("RAZÓN SOCIAL")) {
                        headerFound = true;
                    }
                    continue;
                }
                
                // Dividir usando punto y coma (;)
                String[] tokens = line.split(";");
                
                if (tokens.length >= 10) {
                    Establecimiento est = new Establecimiento();
                    
                    // Mapeo detallado de las 10 columnas oficiales de Osinergmin
                    est.actividad = tokens[1].replace("\"", "").trim();
                    est.telefono = tokens[2].replace("\"", "").trim();   // Código Osinergmin
                    est.nroRegistro = tokens[3].replace("\"", "").trim();
                    est.fechaEmision = tokens[4].replace("\"", "").trim();
                    est.ruc = tokens[5].replace("\"", "").trim();
                    est.nombre = tokens[6].replace("\"", "").trim();     // Razón Social
                    est.direccion = tokens[7].replace("\"", "").trim();
                    est.ubigeo = tokens[8].replace("\"", "").trim();
                    est.placaPrincipal = tokens[9].replace("\"", "").trim();
                    
                    est.sincronizado = true;
                    lista.add(est);
                }
            }
        } catch (Exception e) {
            Log.e("CsvUtil", "Error al procesar CSV expandido", e);
        }
        Log.d("CsvUtil", "Lectura finalizada. Registros procesados: " + lista.size());
        return lista;
    }
}
