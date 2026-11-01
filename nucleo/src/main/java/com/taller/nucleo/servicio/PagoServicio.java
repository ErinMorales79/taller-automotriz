package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.jdbc.PagoDaoJdbc;
import com.taller.nucleo.dao.jdbc.PresupuestoDaoJdbc;
import com.taller.nucleo.entidad.MetodoPago;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Pago;
import com.taller.nucleo.entidad.Presupuesto;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.estado.Cancelada;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.util.Transaccion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;

/**
 * Pagos de las órdenes. El saldo no se guarda: es el total del presupuesto aprobado
 * menos lo pagado. Sin presupuesto aprobado el saldo es cero, así no se cobra lo que no se aprobó.
 */
public final class PagoServicio {

    public Pago registrar(int idOrden, BigDecimal monto, MetodoPago metodo, Usuario usuario) {
        return Transaccion.devolviendo(conexion -> {
            OrdenServicio orden = Consultas.orden(conexion, idOrden);
            Pago pago = new Pago(orden, usuario, monto, metodo);
            if (orden.getEstado() instanceof Cancelada) {
                throw new ReglaNegocioException("No se pueden registrar pagos en una orden cancelada.");
            }
            BigDecimal saldo = saldo(conexion, idOrden);
            if (pago.getMonto().compareTo(saldo) > 0) {
                throw new ReglaNegocioException("El pago de " + pago.getMonto() + " supera el saldo pendiente de "
                        + saldo + " de la orden #" + idOrden + ".");
            }
            return new PagoDaoJdbc(conexion).insertar(pago);
        });
    }

    public BigDecimal saldo(int idOrden) {
        return Transaccion.devolviendo(conexion -> saldo(conexion, idOrden));
    }

    /** Total del presupuesto aprobado menos los pagos (cero si no hay presupuesto aprobado). */
    static BigDecimal saldo(Connection conexion, int idOrden) {
        BigDecimal total = new PresupuestoDaoJdbc(conexion).buscarAprobadoDeOrden(idOrden)
                .map(Presupuesto::total).orElse(BigDecimal.ZERO);
        BigDecimal pagado = new PagoDaoJdbc(conexion).totalPagado(idOrden);
        return total.subtract(pagado).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}
