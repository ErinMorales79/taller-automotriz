package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.RepuestoDao;
import com.taller.nucleo.dao.jdbc.CompraDaoJdbc;
import com.taller.nucleo.dao.jdbc.ConsumoRepuestoDaoJdbc;
import com.taller.nucleo.dao.jdbc.RepuestoDaoJdbc;
import com.taller.nucleo.entidad.Compra;
import com.taller.nucleo.entidad.ConsumoRepuesto;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Repuesto;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.estado.EnReparacion;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.util.Transaccion;

/**
 * Movimientos de inventario. El stock solo cambia al registrar un consumo real en una orden
 * (baja) o una compra (sube); un presupuesto nunca lo toca. Cada movimiento es una transacción.
 */
public final class InventarioServicio {

    /** Regla 3: descuenta el stock en el mismo momento en que registra el consumo. */
    public ConsumoRepuesto registrarConsumo(int idOrden, int idRepuesto, int cantidad, Usuario usuario) {
        return Transaccion.devolviendo(conexion -> {
            OrdenServicio orden = Consultas.orden(conexion, idOrden);
            if (!(orden.getEstado() instanceof EnReparacion)) {
                throw new ReglaNegocioException(
                        "Solo se registran consumos de repuestos en una orden que está en reparación.");
            }
            Repuesto repuesto = Consultas.repuesto(conexion, idRepuesto);
            ConsumoRepuesto consumo = new ConsumoRepuesto(orden, repuesto, usuario, cantidad,
                    repuesto.getPrecioVenta());
            // El descuento es atómico en la base: nunca deja el stock por debajo de cero.
            if (!new RepuestoDaoJdbc(conexion).ajustarStock(idRepuesto, -cantidad)) {
                throw new ReglaNegocioException("Stock insuficiente de " + repuesto.getNombre()
                        + ": hay " + repuesto.getStock() + " y se piden " + cantidad + ".");
            }
            return new ConsumoRepuestoDaoJdbc(conexion).insertar(consumo);
        });
    }

    /** Guarda la compra con sus detalles y suma las cantidades al stock, todo o nada. */
    public Compra registrarCompra(Compra compra) {
        if (compra.getDetalles().isEmpty()) {
            throw new ReglaNegocioException("La compra debe tener al menos un repuesto.");
        }
        return Transaccion.devolviendo(conexion -> {
            RepuestoDao repuestos = new RepuestoDaoJdbc(conexion);
            new CompraDaoJdbc(conexion).insertar(compra);
            for (Compra.DetalleCompra detalle : compra.getDetalles()) {
                if (!repuestos.ajustarStock(detalle.repuesto().getId(), detalle.cantidad())) {
                    throw new ReglaNegocioException("No se pudo sumar el stock de " + detalle.repuesto().getNombre() + ".");
                }
            }
            return compra;
        });
    }
}
