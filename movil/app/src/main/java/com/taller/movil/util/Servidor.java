package com.taller.movil.util;

import okhttp3.HttpUrl;

/** Dirección del servidor de la API: valida lo que escribe el usuario y la deja lista para Retrofit. */
public final class Servidor {

    /** En el emulador de Android, 10.0.2.2 es el computador donde corre la API. */
    public static final String POR_DEFECTO = "http://10.0.2.2:8080/";

    private Servidor() {
    }

    /** Agrega http:// si falta, exige la barra final y rechaza lo que no sea una dirección web. */
    public static String normalizar(String escrito) {
        String texto = escrito == null ? "" : escrito.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Escribe la dirección del servidor, por ejemplo 192.168.1.20:8080.");
        }
        if (!texto.startsWith("http://") && !texto.startsWith("https://")) {
            texto = "http://" + texto;
        }
        HttpUrl url = HttpUrl.parse(texto);
        if (url == null) {
            throw new IllegalArgumentException("La dirección del servidor no es válida. Ejemplo: 192.168.1.20:8080.");
        }
        String normalizada = url.toString();
        return normalizada.endsWith("/") ? normalizada : normalizada + "/";
    }
}
