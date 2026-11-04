package com.taller.movil.red;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Resultado de una llamada a la API: o trae el dato o trae un mensaje en español para el usuario.
 * "sesionVencida" es true cuando el servidor respondió 401 con una sesión abierta.
 */
public final class Resultado<T> {

    static final String MENSAJE_SIN_CONEXION =
            "No se pudo conectar con el servidor. Revisa la dirección y tu conexión.";
    static final String MENSAJE_GENERICO = "El servidor respondió con un error inesperado. Intenta de nuevo.";

    private final T dato;
    private final String mensaje;
    private final boolean sesionVencida;

    private Resultado(T dato, String mensaje, boolean sesionVencida) {
        this.dato = dato;
        this.mensaje = mensaje;
        this.sesionVencida = sesionVencida;
    }

    public boolean esExito() {
        return mensaje == null;
    }

    public T getDato() {
        return dato;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean isSesionVencida() {
        return sesionVencida;
    }

    public static <T> Resultado<T> exito(T dato) {
        return new Resultado<>(dato, null, false);
    }

    public static <T> Resultado<T> error(String mensaje, boolean sesionVencida) {
        return new Resultado<>(null, mensaje, sesionVencida);
    }

    /** Convierte la respuesta HTTP: los errores traen {"mensaje": "..."} que se muestra tal cual. */
    public static <T> Resultado<T> deRespuesta(Response<T> respuesta) {
        if (respuesta.isSuccessful()) {
            return exito(respuesta.body());
        }
        return error(mensajeDelError(respuesta.errorBody()), respuesta.code() == 401);
    }

    public static <T> Resultado<T> deFallo(Throwable fallo) {
        return error(fallo instanceof IOException ? MENSAJE_SIN_CONEXION : MENSAJE_GENERICO, false);
    }

    private static String mensajeDelError(ResponseBody cuerpo) {
        if (cuerpo == null) {
            return MENSAJE_GENERICO;
        }
        try {
            JsonObject json = new Gson().fromJson(cuerpo.string(), JsonObject.class);
            if (json != null && json.has("mensaje") && !json.get("mensaje").isJsonNull()) {
                return json.get("mensaje").getAsString();
            }
        } catch (IOException | JsonParseException | IllegalStateException e) {
            // El cuerpo no es el JSON esperado: se usa el mensaje genérico.
        }
        return MENSAJE_GENERICO;
    }
}
