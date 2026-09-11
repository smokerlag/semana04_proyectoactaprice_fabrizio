package com.example.actapriceproyect.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "establecimientos")
public class Establecimiento {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String nombre;
    public String ruc;
    public String direccion;
    public String telefono; // Usado para Código Osinergmin
    public String ubigeo;
    
    // Nuevos campos estándar Osinergmin
    public String actividad;
    public String nroRegistro;
    public String fechaEmision;
    public String placaPrincipal;
    
    public boolean sincronizado;

    public Establecimiento() {}
}
