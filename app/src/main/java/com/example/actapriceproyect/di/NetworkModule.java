package com.example.actapriceproyect.di;

import com.example.actapriceproyect.network.ApiService;
import com.example.actapriceproyect.utils.SessionManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    // Emulador Android: http://10.0.2.2:3000/
    // Celular físico (misma Wi‑Fi): http://<IP-LAN-PC>:3000/  (ej. http://192.168.0.10:3000/)
    private static final String BASE_URL = "http://10.0.2.2:3000/";

    /** URL base de la API (usada también por WordGenerator para PDF). */
    public static String getBaseUrl() {
        return BASE_URL;
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(SessionManager sessionManager) {
        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String token = sessionManager.fetchAuthToken();
                    if (token == null || token.trim().isEmpty()) {
                        return chain.proceed(original);
                    }
                    Request withAuth = original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(withAuth);
                })
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    public ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }
}
