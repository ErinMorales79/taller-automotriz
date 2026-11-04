package com.taller.movil.red;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Arma el cliente HTTP: agrega el token a cada llamada (si hay sesión) y fija los tiempos de espera. */
public final class ClienteApi {

    private ClienteApi() {
    }

    public static ApiTaller crear(String urlBase, Supplier<String> token) {
        OkHttpClient cliente = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(cadena -> {
                    Request peticion = cadena.request();
                    String actual = token.get();
                    if (actual != null && !actual.isEmpty()) {
                        peticion = peticion.newBuilder().header("Authorization", "Bearer " + actual).build();
                    }
                    return cadena.proceed(peticion);
                })
                .build();
        return new Retrofit.Builder()
                .baseUrl(urlBase)
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiTaller.class);
    }
}
