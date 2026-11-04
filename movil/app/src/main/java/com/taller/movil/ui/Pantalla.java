package com.taller.movil.ui;

import android.app.Activity;
import android.widget.Toast;

import com.taller.movil.AplicacionTaller;
import com.taller.movil.R;
import com.taller.movil.red.Resultado;

/** Comportamiento común de las pantallas ante los resultados de la API. */
final class Pantalla {

    private Pantalla() {
    }

    /**
     * Si el resultado es un error lo muestra y devuelve true. Si la sesión terminó (401),
     * borra el token y vuelve al inicio de sesión.
     */
    static boolean huboError(Activity actividad, Resultado<?> resultado) {
        if (resultado.esExito()) {
            return false;
        }
        if (resultado.isSesionVencida()) {
            ((AplicacionTaller) actividad.getApplication()).sesion().cerrarSesion();
            Toast.makeText(actividad, R.string.sesion_vencida, Toast.LENGTH_LONG).show();
            LoginActivity.reiniciar(actividad);
        } else {
            Toast.makeText(actividad, resultado.getMensaje(), Toast.LENGTH_LONG).show();
        }
        return true;
    }

    /** Las respuestas pueden llegar cuando la pantalla ya se cerró: en ese caso se ignoran. */
    static boolean cerrada(Activity actividad) {
        return actividad.isFinishing() || actividad.isDestroyed();
    }
}
