package com.example.actapriceproyect.di;

import android.content.Context;

import androidx.room.Room;

import com.example.actapriceproyect.database.AppDatabase;
import com.example.actapriceproyect.database.EstablecimientoDao;
import com.example.actapriceproyect.database.FiscalizacionDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "acta_price_db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    public EstablecimientoDao provideEstablecimientoDao(AppDatabase database) {
        return database.establecimientoDao();
    }

    @Provides
    public FiscalizacionDao provideFiscalizacionDao(AppDatabase database) {
        return database.fiscalizacionDao();
    }
}
