package com.taller.nucleo.estado;

public final class Finalizada extends EstadoOrden {

    public Finalizada() {
        super("FINALIZADA");
    }

    @Override
    public EstadoOrden entregar() {
        return new Entregada();
    }
}
