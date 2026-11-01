package com.taller.nucleo.entidad;

import com.taller.nucleo.util.Validar;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Consumo real de un repuesto en una orden: es lo que descuenta el inventario (regla 3). */
public final class ConsumoRepuesto {

    private Integer id;
    private final OrdenServicio orden;
    private final Repuesto repuesto;
    private final Usuario usuario;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private LocalDateTime fecha = LocalDateTime.now();

    public ConsumoRepuesto(OrdenServicio orden, Repuesto repuesto, Usuario usuario, int cantidad,
                           BigDecimal precioUnitario) {
        this.orden = Validar.obligatorio(orden, "orden");
        this.repuesto = Validar.obligatorio(repuesto, "repuesto");
        this.usuario = Validar.obligatorio(usuario, "usuario");
        this.cantidad = Validar.enteroPositivo(cantidad, "cantidad");
        this.precioUnitario = Validar.montoNoNegativo(precioUnitario, "precio unitario");
    }

    public BigDecimal subtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
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

    public Repuesto getRepuesto() {
        return repuesto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = Validar.obligatorio(fecha, "fecha");
    }
}
