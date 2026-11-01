package com.taller.nucleo.entidad;

import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.util.Validar;

import java.time.LocalDateTime;

/** Tarea de un técnico sobre una orden, con su avance. */
public final class Asignacion {

    private Integer id;
    private final OrdenServicio orden;
    private final Tecnico tecnico;
    private LocalDateTime fechaAsignacion = LocalDateTime.now();
    private EstadoTarea estadoTarea = EstadoTarea.PENDIENTE;
    private int porcentajeAvance;

    public Asignacion(OrdenServicio orden, Tecnico tecnico) {
        this.orden = Validar.obligatorio(orden, "orden");
        this.tecnico = Validar.obligatorio(tecnico, "técnico");
    }

    /** El avance y el estado de la tarea se mantienen coherentes entre sí. */
    public void actualizarAvance(int porcentaje) {
        if (porcentaje < 0 || porcentaje > 100) {
            throw new ReglaNegocioException("El avance debe estar entre 0 y 100.");
        }
        porcentajeAvance = porcentaje;
        estadoTarea = porcentaje == 0 ? EstadoTarea.PENDIENTE
                : porcentaje == 100 ? EstadoTarea.TERMINADA : EstadoTarea.EN_CURSO;
    }

    public void restaurar(LocalDateTime fechaAsignacion, EstadoTarea estadoTarea, int porcentajeAvance) {
        this.fechaAsignacion = Validar.obligatorio(fechaAsignacion, "fecha");
        this.estadoTarea = Validar.obligatorio(estadoTarea, "estado de la tarea");
        this.porcentajeAvance = porcentajeAvance;
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

    public Tecnico getTecnico() {
        return tecnico;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public EstadoTarea getEstadoTarea() {
        return estadoTarea;
    }

    public int getPorcentajeAvance() {
        return porcentajeAvance;
    }
}
