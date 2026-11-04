package com.taller.api.seguridad;

/** Credenciales o token inválidos: la API responde 401. */
public class NoAutenticadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}
