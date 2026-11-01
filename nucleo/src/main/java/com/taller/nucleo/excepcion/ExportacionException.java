package com.taller.nucleo.excepcion;

/** Se lanza cuando no se puede escribir un reporte en PDF o Excel. */
public class ExportacionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExportacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
