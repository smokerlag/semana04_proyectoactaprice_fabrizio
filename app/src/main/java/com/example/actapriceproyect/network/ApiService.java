package com.example.actapriceproyect.network;

import com.example.actapriceproyect.model.Establecimiento;
import com.example.actapriceproyect.model.Fiscalizacion;
import com.example.actapriceproyect.model.LoginRequest;
import com.example.actapriceproyect.model.LoginResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("establecimientos")
    Call<List<Establecimiento>> getEstablecimientos();

    @POST("establecimientos")
    Call<Establecimiento> crearEstablecimiento(@Body Establecimiento establecimiento);

    @GET("fiscalizaciones")
    Call<List<Fiscalizacion>> getFiscalizaciones();

    @POST("fiscalizaciones")
    Call<Fiscalizacion> registrarFiscalizacion(@Body Fiscalizacion fiscalizacion);
}
