package com.taller.nucleo.entidad;

import com.taller.nucleo.estado.EstadoOrden;
import com.taller.nucleo.estado.Recibida;
import com.taller.nucleo.excepcion.ReglaNegocioException;
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
    private Usuario autorizaDuplicada;
    private Usuario autorizaCredito;

    public OrdenServicio(Vehiculo vehiculo, TipoAtencion tipoAtencion, Prioridad prioridad,
                         Usuario usuarioRegistro) {
        this.vehiculo = Validar.obligatorio(vehiculo, "vehículo");
        this.tipoAtencion = Validar.obligatorio(tipoAtencion, "tipo de atención");
        this.prioridad = Validar.obligatorio(prioridad, "prioridad");
        this.usuarioRegistro = Validar.obligatorio(usuarioRegistro, "usuario que registra");
    }

    /** Solo un ADMINISTRADOR puede autorizar una segunda orden activa del mismo tipo (regla 1). */
    public void autorizarDuplicada(Usuario administrador) {
        autorizaDuplicada = exigirAdministrador(administrador, "autorizar una orden duplicada");
    }

    /** Solo un ADMINISTRADOR puede autorizar la entrega con saldo pendiente (regla 4). */
    public void autorizarCredito(Usuario administrador) {
        autorizaCredito = exigirAdministrador(administrador, "autorizar la entrega a crédito");
    }

    public boolean tieneCreditoAutorizado() {
        return autorizaCredito != null;
    }

    private static Usuario exigirAdministrador(Usuario usuario, String accion) {
        Validar.obligatorio(usuario, "usuario que autoriza");
        if (!usuario.puedeAutorizarExcepciones()) {
            throw new ReglaNegocioException("Solo un administrador puede " + accion + ".");
        }
        return usuario;
    }

    /** Reconstruye los datos guardados; solo lo usa la capa DAO. No pasa por las transiciones. */
    public void restaurar(EstadoOrden estado, LocalDateTime fechaIngreso, LocalDateTime fechaEntrega,
                          Usuario autorizaDuplicada, Usuario autorizaCredito) {
        this.estado = Validar.obligatorio(estado, "estado");
        this.fechaIngreso = Validar.obligatorio(fechaIngreso, "fecha de ingreso");
        this.fechaEntrega = fechaEntrega;
        this.autorizaDuplicada = autorizaDuplicada;
        this.autorizaCredito = autorizaCredito;
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

    public Usuario getAutorizaDuplicada() {
        return autorizaDuplicada;
    }

    public Usuario getAutorizaCredito() {
        return autorizaCredito;
    }

    @Override
    public String toString() {
        return "Orden " + (id == null ? "nueva" : "#" + id) + " - " + vehiculo + " [" + estado + "]";
    }
}
