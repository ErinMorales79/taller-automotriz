package com.taller.nucleo.estado;

/** Estado final: no permite ninguna transición. */
public final class Cancelada extends EstadoOrden {

    public Cancelada() {
        super("CANCELADA");
    }

    @Override
    public boolean esActiva() {
        return false;
    }
}
