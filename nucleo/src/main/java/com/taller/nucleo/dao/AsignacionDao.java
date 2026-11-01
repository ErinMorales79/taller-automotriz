package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.Asignacion;

import java.util.List;

public interface AsignacionDao extends DaoModificable<Asignacion> {

    List<Asignacion> listarPorOrden(int idOrden);

    /** Tareas de un técnico (las que usará la app móvil). */
    List<Asignacion> listarPorTecnico(int idTecnico);
}
