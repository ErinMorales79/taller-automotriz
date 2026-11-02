package com.taller.escritorio.util;

import com.taller.nucleo.excepcion.ReglaNegocioException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** Conversión entre lo que escribe el usuario y los tipos del dominio, y viceversa. */
public final class Formato {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formato() {
    }

    public static String dinero(BigDecimal monto) {
        return monto == null ? "-" : String.format(Locale.US, "%,.2f", monto);
    }

    public static String fechaHora(LocalDateTime fecha) {
        return fecha == null ? "-" : FECHA_HORA.format(fecha);
    }

    /** Convierte un nombre como EN_REPARACION en "En reparación". */
    public static String legible(Object valor) {
        if (valor == null) {
            return "-";
        }
        String texto = valor.toString().replace('_', ' ').toLowerCase(Locale.ROOT)
                .replace("diagnostico", "diagnóstico").replace("aprobacion", "aprobación")
                .replace("reparacion", "reparación").replace("atencion", "atención");
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    public static BigDecimal decimal(String texto, String campo) {
        try {
            return new BigDecimal(texto.trim().replace(",", ""));
        } catch (NumberFormatException | NullPointerException e) {
            throw new ReglaNegocioException("El campo " + campo + " debe ser un número, por ejemplo 125.50.");
        }
    }

    public static int entero(String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new ReglaNegocioException("El campo " + campo + " debe ser un número entero.");
        }
    }

    /** Fecha dd/MM/yyyy; vacío significa "sin fecha". */
    public static LocalDate fechaOpcional(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim(), FECHA);
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("El campo " + campo + " debe tener el formato dd/mm/aaaa, por ejemplo 31/01/2026.");
        }
    }
}
