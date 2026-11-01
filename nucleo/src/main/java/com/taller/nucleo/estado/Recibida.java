package com.taller.nucleo.estado;

public final class Recibida extends EstadoOrden {

    public Recibida() {
        super("RECIBIDA");
    }

    @Override
    public EstadoOrden iniciarDiagnostico() {
        return new EnDiagnostico();
    }

    @Override
    public EstadoOrden cancelar() {
        return new Cancelada();
    }
}
