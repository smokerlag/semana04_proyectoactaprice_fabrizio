package com.example.actapriceproyect.repository;

import com.example.actapriceproyect.database.EstablecimientoDao;
import com.example.actapriceproyect.database.FiscalizacionDao;
import com.example.actapriceproyect.model.Establecimiento;
import com.example.actapriceproyect.model.Fiscalizacion;
import com.example.actapriceproyect.network.ApiService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActaRepository {
    private final EstablecimientoDao establecimientoDao;
    private final FiscalizacionDao fiscalizacionDao;
    private final ApiService apiService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Inject
    public ActaRepository(EstablecimientoDao establecimientoDao, FiscalizacionDao fiscalizacionDao, ApiService apiService) {
        this.establecimientoDao = establecimientoDao;
        this.fiscalizacionDao = fiscalizacionDao;
        this.apiService = apiService;
    }

    // CRUD Establecimientos
    public void insertEstablecimiento(Establecimiento establecimiento) {
        executorService.execute(() -> {
            establecimientoDao.insert(establecimiento);
            syncEstablecimientos();
        });
    }

    public List<Establecimiento> getAllEstablecimientos() {
        return establecimientoDao.getAll();
    }

    public Establecimiento getEstablecimientoById(int id) {
        return establecimientoDao.getById(id);
    }

    // Importación masiva desde CSV
    public void revisarYPrecargarDesdeCsv(android.content.Context context, Runnable onComplete) {
        executorService.execute(() -> {
            List<Establecimiento> existentes = establecimientoDao.getAll();
            if (existentes == null || existentes.isEmpty()) {
                List<Establecimiento> desdeCsv = com.example.actapriceproyect.utils.CsvUtil.cargarEstablecimientosDesdeCsv(context);
                for (Establecimiento est : desdeCsv) {
                    establecimientoDao.insert(est);
                }
            }
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    // CRUD Fiscalizaciones
    public void insertFiscalizacion(Fiscalizacion fiscalizacion) {
        executorService.execute(() -> {
            fiscalizacionDao.insert(fiscalizacion);
            syncFiscalizaciones();
        });
    }

    // Lógica de Sincronización
    public void syncEstablecimientos() {
        executorService.execute(() -> {
            List<Establecimiento> noSincronizados = establecimientoDao.getNoSincronizados();
            for (Establecimiento est : noSincronizados) {
                apiService.crearEstablecimiento(est).enqueue(new Callback<Establecimiento>() {
                    @Override
                    public void onResponse(Call<Establecimiento> call, Response<Establecimiento> response) {
                        if (response.isSuccessful()) {
                            executorService.execute(() -> {
                                est.sincronizado = true;
                                establecimientoDao.update(est);
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<Establecimiento> call, Throwable t) {
                        // Manejar error de red
                    }
                });
            }
        });
    }

    public void syncFiscalizaciones() {
        executorService.execute(() -> {
            List<Fiscalizacion> noSincronizados = fiscalizacionDao.getNoSincronizados();
            for (Fiscalizacion fis : noSincronizados) {
                apiService.registrarFiscalizacion(fis).enqueue(new Callback<Fiscalizacion>() {
                    @Override
                    public void onResponse(Call<Fiscalizacion> call, Response<Fiscalizacion> response) {
                        if (response.isSuccessful()) {
                            executorService.execute(() -> {
                                fis.sincronizado = true;
                                fiscalizacionDao.update(fis);
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<Fiscalizacion> call, Throwable t) {
                        // Manejar error
                    }
                });
            }
        });
    }
}
