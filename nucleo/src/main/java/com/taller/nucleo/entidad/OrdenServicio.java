package com.taller.nucleo.entidad;

import com.taller.nucleo.estado.EstadoOrden;
import com.taller.nucleo.estado.Recibida;
import com.taller.nucleo.util.Validar;

import java.time.LocalDateTime;

/**
 * Las transiciones de estado se delegan en el objeto EstadoOrden actual. Las reglas que
 * dependen de otros datos (presupuesto aprobado, saldo, etc.) las valida el servicio.
 */
public final class OrdenServicio {

    private Integer id;
    private Vehiculo vehiculo;
    private TipoAtencion tipoAtencion;
    private Prioridad prioridad;
    private EstadoOrden estado = new Recibida();
    private LocalDateTime fechaIngreso = LocalDateTime.now();
    private LocalDateTime fechaEntrega;
    private Usuario usuarioRegistro;

    public OrdenServicio(Vehiculo vehiculo, TipoAtencion tipoAtencion, Prioridad prioridad,
                         Usuario usuarioRegistro) {
        this.vehiculo = Validar.obligatorio(vehiculo, "vehículo");
        this.tipoAtencion = Validar.obligatorio(tipoAtencion, "tipo de atención");
        this.prioridad = Validar.obligatorio(prioridad, "prioridad");
        this.usuarioRegistro = Validar.obligatorio(usuarioRegistro, "usuario que registra");
    }

    public boolean estaActiva() {
        return estado.esActiva();
    }

    public void iniciarDiagnostico() {
        estado = estado.iniciarDiagnostico();
    }

    public void enviarAAprobacion() {
        estado = estado.enviarAAprobacion();
    }

    public void volverADiagnostico() {
        estado = estado.volverADiagnostico();
    }

    public void iniciarReparacion() {
        estado = estado.iniciarReparacion();
    }

    public void finalizar() {
        estado = estado.finalizar();
    }

    public void entregar() {
        estado = estado.entregar();
        fechaEntrega = LocalDateTime.now();
    }

    public void cancelar() {
        estado = estado.cancelar();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public TipoAtencion getTipoAtencion() {
        return tipoAtencion;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Prioridad prioridad) {
        this.prioridad = Validar.obligatorio(prioridad, "prioridad");
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public Usuario getUsuarioRegistro() {
        return usuarioRegistro;
    }

    @Override
    public String toString() {
        return "Orden " + (id == null ? "nueva" : "#" + id) + " - " + vehiculo + " [" + estado + "]";
    }
}
