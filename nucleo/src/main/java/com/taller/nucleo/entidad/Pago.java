package com.taller.nucleo.entidad;

import com.taller.nucleo.util.Validar;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Pago {

    private Integer id;
    private final OrdenServicio orden;
    private final Usuario usuario;
    private final BigDecimal monto;
    private final MetodoPago metodo;
    private LocalDateTime fecha = LocalDateTime.now();

    public Pago(OrdenServicio orden, Usuario usuario, BigDecimal monto, MetodoPago metodo) {
        this.orden = Validar.obligatorio(orden, "orden");
        this.usuario = Validar.obligatorio(usuario, "usuario");
        this.monto = Validar.montoPositivo(monto, "monto");
        this.metodo = Validar.obligatorio(metodo, "método de pago");
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

    public Usuario getUsuario() {
        return usuario;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = Validar.obligatorio(fecha, "fecha");
    }
}
