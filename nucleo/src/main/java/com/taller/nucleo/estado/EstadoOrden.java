package com.taller.nucleo.estado;

import com.taller.nucleo.excepcion.ReglaNegocioException;

/**
 * Patrón State: cada estado de la orden es una subclase que sobrescribe solo las
 * transiciones que permite. Las demás caen en esta clase base y se rechazan.
 * Cada transición devuelve el estado siguiente.
 */
public abstract class EstadoOrden {

    private final String nombre;

    protected EstadoOrden(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    /** Una orden está activa mientras no esté ENTREGADA ni CANCELADA. */
    public boolean esActiva() {
        return true;
    }

    public EstadoOrden iniciarDiagnostico() {
        throw rechazo("iniciar el diagnóstico");
    }

    public EstadoOrden enviarAAprobacion() {
        throw rechazo("enviar el presupuesto a aprobación");
    }

    public EstadoOrden volverADiagnostico() {
        throw rechazo("volver a diagnóstico");
    }

    public EstadoOrden iniciarReparacion() {
        throw rechazo("iniciar la reparación");
    }

    public EstadoOrden finalizar() {
        throw rechazo("finalizar la orden");
    }

    public EstadoOrden entregar() {
        throw rechazo("entregar el vehículo");
    }

    public EstadoOrden cancelar() {
        throw rechazo("cancelar la orden");
    }

    /** Reconstruye el estado a partir del texto guardado en la base de datos. */
    public static EstadoOrden desde(String nombre) {
        if (nombre == null) {
            throw new ReglaNegocioException("El estado de la orden es obligatorio.");
        }
        return switch (nombre) {
            case "RECIBIDA" -> new Recibida();
            case "EN_DIAGNOSTICO" -> new EnDiagnostico();
            case "ESPERANDO_APROBACION" -> new EsperandoAprobacion();
            case "EN_REPARACION" -> new EnReparacion();
            case "FINALIZADA" -> new Finalizada();
            case "ENTREGADA" -> new Entregada();
            case "CANCELADA" -> new Cancelada();
            default -> throw new ReglaNegocioException("Estado de orden desconocido: " + nombre + ".");
        };
    }

    private ReglaNegocioException rechazo(String accion) {
        return new ReglaNegocioException(
                "No se puede " + accion + " cuando la orden está en estado " + nombre + ".");
    }

    @Override
    public boolean equals(Object otro) {
        return otro instanceof EstadoOrden estado && nombre.equals(estado.nombre);
    }

    @Override
    public int hashCode() {
        return nombre.hashCode();
    }

    @Override
    public String toString() {
        return nombre;
    }
}
