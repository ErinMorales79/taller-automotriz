package com.taller.movil.red;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Ejecuta una llamada sin bloquear la pantalla y entrega un Resultado (Retrofit responde en el hilo principal en Android). */
public final class Llamadas {

    public interface Receptor<T> {
        void alTerminar(Resultado<T> resultado);
    }

    private Llamadas() {
    }

    public static <T> void ejecutar(Call<T> llamada, Receptor<T> receptor) {
        llamada.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> llamada, Response<T> respuesta) {
                receptor.alTerminar(Resultado.deRespuesta(respuesta));
            }

            @Override
            public void onFailure(Call<T> llamada, Throwable fallo) {
                receptor.alTerminar(Resultado.deFallo(fallo));
            }
        });
    }
}
