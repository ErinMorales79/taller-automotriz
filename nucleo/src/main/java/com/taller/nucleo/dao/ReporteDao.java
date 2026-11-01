package com.taller.nucleo.dao;

import com.taller.nucleo.reporte.Reporte;

import java.time.LocalDateTime;

/**
 * Consultas de los reportes obligatorios. Los rangos de fechas son opcionales (null = sin límite);
 * "hastaExclusivo" no incluye el instante indicado.
 */
public interface ReporteDao {

    /** Reporte 1: cantidad de órdenes por estado y su antigüedad en días. */
    Reporte ordenesPorEstado(LocalDateTime desde, LocalDateTime hastaExclusivo);

    /** Reporte 2: tareas, terminadas, órdenes y avance promedio de cada técnico. */
    Reporte productividadPorTecnico(LocalDateTime desde, LocalDateTime hastaExclusivo);

    /** Reporte 3: repuestos con más unidades consumidas (los primeros "limite"). */
    Reporte repuestosMasConsumidos(LocalDateTime desde, LocalDateTime hastaExclusivo, int limite);

    /** Reporte 4: ingresos por servicio, solo de órdenes finalizadas o entregadas con presupuesto aprobado. */
    Reporte ingresosPorServicio(LocalDateTime desde, LocalDateTime hastaExclusivo);

    /** Reporte 5: todas las órdenes de un vehículo, de la más reciente a la más antigua. */
    Reporte historialDeVehiculo(int idVehiculo);
}
