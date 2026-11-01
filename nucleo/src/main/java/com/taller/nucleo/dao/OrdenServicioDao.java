package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.OrdenServicio;

import java.util.List;

/** Las órdenes no se eliminan: se cancelan (por eso no es DaoModificable). */
public interface OrdenServicioDao extends Dao<OrdenServicio> {

    /** Guarda estado, prioridad, fechas y autorizaciones actuales de la orden. */
    void actualizar(OrdenServicio orden);

    /** Regla 1: ¿el vehículo ya tiene una orden activa de este tipo de atención? */
    boolean existeActiva(int idVehiculo, int idTipoAtencion);

    /**
     * Filtros opcionales (null o vacío = sin filtrar): nombre del estado exacto y
     * texto contenido en la placa o en el nombre del propietario.
     */
    List<OrdenServicio> buscar(String estado, String texto);

    List<OrdenServicio> listarPorVehiculo(int idVehiculo);
}
