package com.taller.nucleo.entidad;

import com.taller.nucleo.util.Validar;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Compra a un proveedor. Compone sus detalles (maestro-detalle) y aumenta el stock al guardarse. */
public final class Compra {

    public record DetalleCompra(Repuesto repuesto, int cantidad, BigDecimal costoUnitario) {

        public DetalleCompra {
            Validar.obligatorio(repuesto, "repuesto");
            Validar.enteroPositivo(cantidad, "cantidad");
            costoUnitario = Validar.montoNoNegativo(costoUnitario, "costo unitario");
        }

        public BigDecimal subtotal() {
            return costoUnitario.multiply(BigDecimal.valueOf(cantidad));
        }
    }

    private Integer id;
    private final Proveedor proveedor;
    private final Usuario usuario;
    private LocalDateTime fecha = LocalDateTime.now();
    private final List<DetalleCompra> detalles = new ArrayList<>();

    public Compra(Proveedor proveedor, Usuario usuario) {
        this.proveedor = Validar.obligatorio(proveedor, "proveedor");
        this.usuario = Validar.obligatorio(usuario, "usuario");
    }

    public void agregarDetalle(Repuesto repuesto, int cantidad, BigDecimal costoUnitario) {
        detalles.add(new DetalleCompra(repuesto, cantidad, costoUnitario));
    }

    public BigDecimal total() {
        return detalles.stream().map(DetalleCompra::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = Validar.obligatorio(fecha, "fecha");
    }

    public List<DetalleCompra> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }
}
