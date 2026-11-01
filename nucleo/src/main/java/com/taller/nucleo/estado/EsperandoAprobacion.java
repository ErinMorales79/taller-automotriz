package com.taller.nucleo.estado;

/** Aquí queda la orden si el presupuesto es rechazado: recepción decide volver o cancelar. */
public final class EsperandoAprobacion extends EstadoOrden {

    public EsperandoAprobacion() {
        super("ESPERANDO_APROBACION");
    }

    @Override
    public EstadoOrden iniciarReparacion() {
        return new EnReparacion();
    }

    @Override
    public EstadoOrden volverADiagnostico() {
        return new EnDiagnostico();
    }

    @Override
    public EstadoOrden cancelar() {
        return new Cancelada();
    }
}
