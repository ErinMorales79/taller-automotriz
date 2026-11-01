package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.Pago;

import java.math.BigDecimal;
import java.util.List;

public interface PagoDao extends Dao<Pago> {

    List<Pago> listarPorOrden(int idOrden);

    /** Total pagado de la orden (cero si no hay pagos). */
    BigDecimal totalPagado(int idOrden);
}
