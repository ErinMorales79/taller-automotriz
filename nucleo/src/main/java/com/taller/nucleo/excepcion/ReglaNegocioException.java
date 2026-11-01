package com.taller.nucleo.excepcion;

/** Se lanza cuando una operación incumple una regla de negocio o una validación. */
public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
