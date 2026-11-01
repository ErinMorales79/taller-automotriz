package com.taller.nucleo.estado;

public final class EnReparacion extends EstadoOrden {

    public EnReparacion() {
        super("EN_REPARACION");
    }

    @Override
    public EstadoOrden finalizar() {
        return new Finalizada();
    }
}
