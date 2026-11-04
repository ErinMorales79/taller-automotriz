package com.taller.movil;

import android.app.Application;

import com.taller.movil.datos.AlmacenSesion;
import com.taller.movil.red.ApiTaller;
import com.taller.movil.red.ClienteApi;

/** Punto común de la app: la sesión guardada y el cliente de la API (se vuelve a crear si cambia el servidor). */
public class AplicacionTaller extends Application {

    private AlmacenSesion sesion;
    private ApiTaller api;
    private String servidorDeLaApi;

    public AlmacenSesion sesion() {
        if (sesion == null) {
            sesion = new AlmacenSesion(this);
        }
        return sesion;
    }

    public synchronized ApiTaller api() {
        String servidor = sesion().servidor();
        if (api == null || !servidor.equals(servidorDeLaApi)) {
            api = ClienteApi.crear(servidor, () -> sesion().token());
            servidorDeLaApi = servidor;
        }
        return api;
    }
}
