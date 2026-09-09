package com.example.actapriceproyect.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.actapriceproyect.model.Establecimiento;

import java.util.List;

@Dao
public interface EstablecimientoDao {
    @Query("SELECT * FROM establecimientos")
    List<Establecimiento> getAll();

    @Query("SELECT * FROM establecimientos WHERE id = :id")
    Establecimiento getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Establecimiento establecimiento);

    @Update
    void update(Establecimiento establecimiento);

    @Delete
    void delete(Establecimiento establecimiento);

    @Query("SELECT * FROM establecimientos WHERE sincronizado = 0")
    List<Establecimiento> getNoSincronizados();
}
