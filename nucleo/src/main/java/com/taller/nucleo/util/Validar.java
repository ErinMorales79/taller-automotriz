package com.taller.nucleo.util;

import com.taller.nucleo.excepcion.ReglaNegocioException;

/** Validaciones de entrada compartidas por las entidades. */
public final class Validar {

    private Validar() {
    }

    public static String textoObligatorio(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("El campo " + campo + " es obligatorio.");
        }
        return limitar(valor.trim(), campo, maximo);
    }

    /** Devuelve null si el valor viene vacío. */
    public static String textoOpcional(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return limitar(valor.trim(), campo, maximo);
    }

    public static int enteroMinimo(int valor, int minimo, String campo) {
        if (valor < minimo) {
            throw new ReglaNegocioException(
                    "El campo " + campo + " no puede ser menor que " + minimo + ".");
        }
        return valor;
    }

    public static <T> T obligatorio(T valor, String campo) {
        if (valor == null) {
            throw new ReglaNegocioException("El campo " + campo + " es obligatorio.");
        }
        return valor;
    }

    private static String limitar(String texto, String campo, int maximo) {
        if (texto.length() > maximo) {
            throw new ReglaNegocioException(
                    "El campo " + campo + " no puede superar los " + maximo + " caracteres.");
        }
        return texto;
    }
}
