package com.taller.nucleo.entidad;

import com.taller.nucleo.util.Validar;

import java.time.LocalDateTime;

/** Un cambio de estado de una orden: de qué estado a cuál, cuándo y quién (regla 5). */
public final class HistorialEstadoOrden {

    private Integer id;
    private final OrdenServicio orden;
    private final String estadoAnterior;
    private final String estadoNuevo;
    private LocalDateTime fecha = LocalDateTime.now();
    private final Usuario responsable;

    /** estadoAnterior es null cuando se registra la creación de la orden. */
    public HistorialEstadoOrden(OrdenServicio orden, String estadoAnterior, String estadoNuevo,
                                Usuario responsable) {
        this.orden = Validar.obligatorio(orden, "orden");
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = Validar.textoObligatorio(estadoNuevo, "estado nuevo", 25);
        this.responsable = Validar.obligatorio(responsable, "responsable");
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public OrdenServicio getOrden() {
        return orden;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public String getEstadoNuevo() {
        return estadoNuevo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = Validar.obligatorio(fecha, "fecha");
    }

    public Usuario getResponsable() {
        return responsable;
    }

    @Override
    public String toString() {
        return (estadoAnterior == null ? "(nueva)" : estadoAnterior) + " -> " + estadoNuevo
                + " por " + responsable.getNombreUsuario();
    }
}
