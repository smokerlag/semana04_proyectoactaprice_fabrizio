package com.example.actapriceproyect.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.actapriceproyect.model.Establecimiento;
import com.example.actapriceproyect.model.Fiscalizacion;

@Database(entities = {Establecimiento.class, Fiscalizacion.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    public abstract EstablecimientoDao establecimientoDao();
    public abstract FiscalizacionDao fiscalizacionDao();
}
