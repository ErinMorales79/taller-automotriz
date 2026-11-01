package com.taller.nucleo.entidad;

import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.util.Validar;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encabezado del presupuesto. Compone sus detalles de mano de obra y de repuestos
 * (maestro-detalle): no existen sin él.
 */
public final class Presupuesto {

    /** Línea de mano de obra. */
    public record DetalleServicio(Servicio servicio, BigDecimal horas, BigDecimal precioUnitario) {

        public DetalleServicio {
            Validar.obligatorio(servicio, "servicio");
            horas = Validar.montoPositivo(horas, "horas");
            precioUnitario = Validar.montoNoNegativo(precioUnitario, "precio unitario");
        }

        public BigDecimal subtotal() {
            return horas.multiply(precioUnitario);
        }
    }

    /** Línea de repuestos. Presupuestar un repuesto NO descuenta stock (regla 3). */
    public record DetalleRepuesto(Repuesto repuesto, int cantidad, BigDecimal precioUnitario) {

        public DetalleRepuesto {
            Validar.obligatorio(repuesto, "repuesto");
            Validar.enteroPositivo(cantidad, "cantidad");
            precioUnitario = Validar.montoNoNegativo(precioUnitario, "precio unitario");
        }

        public BigDecimal subtotal() {
            return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        }
    }

    private Integer id;
    private final OrdenServicio orden;
    private EstadoPresupuesto estado = EstadoPresupuesto.PENDIENTE;
    private LocalDateTime fecha = LocalDateTime.now();
    private Usuario aprobador;
    private final List<DetalleServicio> servicios = new ArrayList<>();
    private final List<DetalleRepuesto> repuestos = new ArrayList<>();

    public Presupuesto(OrdenServicio orden) {
        this.orden = Validar.obligatorio(orden, "orden");
    }

    public void agregarServicio(Servicio servicio, BigDecimal horas) {
        Validar.obligatorio(servicio, "servicio");
        exigirPendiente();
        servicios.add(new DetalleServicio(servicio, horas, servicio.getPrecioBase()));
    }

    public void agregarRepuesto(Repuesto repuesto, int cantidad) {
        Validar.obligatorio(repuesto, "repuesto");
        exigirPendiente();
        repuestos.add(new DetalleRepuesto(repuesto, cantidad, repuesto.getPrecioVenta()));
    }

    /** Se usa al reconstruir desde la base de datos (conserva el precio guardado). */
    public void restaurarDetalle(DetalleServicio detalle) {
        servicios.add(detalle);
    }

    public void restaurarDetalle(DetalleRepuesto detalle) {
        repuestos.add(detalle);
    }

    public void restaurar(EstadoPresupuesto estado, LocalDateTime fecha, Usuario aprobador) {
        this.estado = Validar.obligatorio(estado, "estado");
        this.fecha = Validar.obligatorio(fecha, "fecha");
        this.aprobador = aprobador;
    }

    public BigDecimal total() {
        BigDecimal suma = BigDecimal.ZERO;
        for (DetalleServicio detalle : servicios) {
            suma = suma.add(detalle.subtotal());
        }
        for (DetalleRepuesto detalle : repuestos) {
            suma = suma.add(detalle.subtotal());
        }
        return suma.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public boolean estaVacio() {
        return servicios.isEmpty() && repuestos.isEmpty();
    }

    public void aprobar(Usuario responsable) {
        exigirPendiente();
        Validar.obligatorio(responsable, "usuario que aprueba");
        estado = EstadoPresupuesto.APROBADO;
        aprobador = responsable;
    }

    public void rechazar(Usuario responsable) {
        exigirPendiente();
        Validar.obligatorio(responsable, "usuario que rechaza");
        estado = EstadoPresupuesto.RECHAZADO;
        aprobador = responsable;
    }

    private void exigirPendiente() {
        if (estado != EstadoPresupuesto.PENDIENTE) {
            throw new ReglaNegocioException("El presupuesto ya fue " + estado.name().toLowerCase()
                    + " y no puede modificarse.");
        }
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

    public EstadoPresupuesto getEstado() {
        return estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Usuario getAprobador() {
        return aprobador;
    }

    public List<DetalleServicio> getServicios() {
        return Collections.unmodifiableList(servicios);
    }

    public List<DetalleRepuesto> getRepuestos() {
        return Collections.unmodifiableList(repuestos);
    }

    @Override
    public String toString() {
        return "Presupuesto " + (id == null ? "nuevo" : "#" + id) + " [" + estado + "] " + total();
    }
}
