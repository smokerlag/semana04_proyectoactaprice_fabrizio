package com.example.actapriceproyect.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.actapriceproyect.model.Fiscalizacion;

import java.util.List;

@Dao
public interface FiscalizacionDao {
    @Query("SELECT * FROM fiscalizaciones")
    List<Fiscalizacion> getAll();

    @Query("SELECT * FROM fiscalizaciones WHERE establecimientoId = :estId")
    List<Fiscalizacion> getByEstablecimiento(int estId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Fiscalizacion fiscalizacion);

    @Update
    void update(Fiscalizacion fiscalizacion);

    @Delete
    void delete(Fiscalizacion fiscalizacion);

    @Query("SELECT * FROM fiscalizaciones WHERE sincronizado = 0")
    List<Fiscalizacion> getNoSincronizados();
}
