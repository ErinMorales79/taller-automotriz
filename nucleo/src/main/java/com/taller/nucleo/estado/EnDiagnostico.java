package com.taller.nucleo.estado;

public final class EnDiagnostico extends EstadoOrden {

    public EnDiagnostico() {
        super("EN_DIAGNOSTICO");
    }

    @Override
    public EstadoOrden enviarAAprobacion() {
        return new EsperandoAprobacion();
    }

    @Override
    public EstadoOrden cancelar() {
        return new Cancelada();
    }
}
