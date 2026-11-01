package com.taller.nucleo.excepcion;

/** Se lanza cuando el usuario no tiene permiso para la acción, o sus credenciales no son válidas. */
public class AccesoDenegadoException extends ReglaNegocioException {

    private static final long serialVersionUID = 1L;

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
