package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.HistorialEstadoOrden;

import java.util.List;

/** El historial solo se agrega y se consulta: nunca se edita ni se borra (regla 5). */
public interface HistorialEstadoOrdenDao extends Dao<HistorialEstadoOrden> {

    /** Cronológico: del cambio más antiguo al más reciente. */
    List<HistorialEstadoOrden> listarPorOrden(int idOrden);
}
