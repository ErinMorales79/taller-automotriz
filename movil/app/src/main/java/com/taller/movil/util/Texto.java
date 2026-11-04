package com.taller.movil.util;

import java.util.Locale;

/** Textos para mostrar al usuario. */
public final class Texto {

    private Texto() {
    }

    /** Convierte un nombre del servidor como EN_REPARACION en "En reparación". */
    public static String legible(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "-";
        }
        String texto = valor.replace('_', ' ').toLowerCase(Locale.ROOT)
                .replace("diagnostico", "diagnóstico").replace("aprobacion", "aprobación")
                .replace("reparacion", "reparación").replace("atencion", "atención");
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    /** "2026-10-29T18:24:00" pasa a "29/10/2026 18:24"; si no se entiende, se deja como llegó. */
    public static String fechaHora(String iso) {
        if (iso == null || iso.length() < 16 || iso.charAt(10) != 'T') {
            return iso == null ? "-" : iso;
        }
        return iso.substring(8, 10) + "/" + iso.substring(5, 7) + "/" + iso.substring(0, 4) + " " + iso.substring(11, 16);
    }
}
