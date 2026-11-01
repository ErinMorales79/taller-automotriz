package com.taller.nucleo.estado;

/** Estado final: no permite ninguna transición. */
public final class Entregada extends EstadoOrden {

    public Entregada() {
        super("ENTREGADA");
    }

    @Override
    public boolean esActiva() {
        return false;
    }
}
