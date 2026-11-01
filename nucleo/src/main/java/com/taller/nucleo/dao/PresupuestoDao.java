package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.Presupuesto;

import java.util.List;
import java.util.Optional;

/** Un presupuesto se guarda junto con sus detalles; llamar dentro de una transacción. */
public interface PresupuestoDao extends Dao<Presupuesto> {

    /** Guarda el estado y el aprobador (los detalles no cambian una vez creado). */
    void actualizarEstado(Presupuesto presupuesto);

    List<Presupuesto> listarPorOrden(int idOrden);

    /** Regla 2 y saldo: el presupuesto aprobado de la orden, si existe. */
    Optional<Presupuesto> buscarAprobadoDeOrden(int idOrden);
}
