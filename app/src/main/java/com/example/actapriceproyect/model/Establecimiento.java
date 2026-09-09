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
    public String telefono;
    
    public boolean sincronizado;

    public Establecimiento() {}
}
