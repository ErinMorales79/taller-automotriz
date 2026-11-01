package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.jdbc.ReporteDaoJdbc;
import com.taller.nucleo.dao.jdbc.VehiculoDaoJdbc;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.reporte.ExportadorReportes;
import com.taller.nucleo.reporte.Reporte;
import com.taller.nucleo.seguridad.Accion;
import com.taller.nucleo.seguridad.Permisos;
import com.taller.nucleo.util.Transaccion;
import com.taller.nucleo.util.Validar;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.BiFunction;

/**
 * Reportes obligatorios. Se leen siempre de la base de datos, solo los ven los roles con
 * permiso VER_REPORTES y se pueden mostrar en pantalla o exportar a PDF y Excel.
 * En los rangos, "desde" y "hasta" son fechas inclusivas y cualquiera puede ser null.
 */
public final class ReporteServicio {

    private static final int LIMITE_MAXIMO = 100;

    public Reporte ordenesPorEstado(Usuario usuario, LocalDate desde, LocalDate hasta) {
        return consultar(usuario, desde, hasta, (d, h) -> Transaccion.devolviendo(
                conexion -> new ReporteDaoJdbc(conexion).ordenesPorEstado(d, h)));
    }

    public Reporte productividadPorTecnico(Usuario usuario, LocalDate desde, LocalDate hasta) {
        return consultar(usuario, desde, hasta, (d, h) -> Transaccion.devolviendo(
                conexion -> new ReporteDaoJdbc(conexion).productividadPorTecnico(d, h)));
    }

    public Reporte repuestosMasConsumidos(Usuario usuario, LocalDate desde, LocalDate hasta, int limite) {
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new ReglaNegocioException("El límite debe estar entre 1 y " + LIMITE_MAXIMO + ".");
        }
        return consultar(usuario, desde, hasta, (d, h) -> Transaccion.devolviendo(
                conexion -> new ReporteDaoJdbc(conexion).repuestosMasConsumidos(d, h, limite)));
    }

    public Reporte ingresosPorServicio(Usuario usuario, LocalDate desde, LocalDate hasta) {
        return consultar(usuario, desde, hasta, (d, h) -> Transaccion.devolviendo(
                conexion -> new ReporteDaoJdbc(conexion).ingresosPorServicio(d, h)));
    }

    public Reporte historialDeVehiculo(Usuario usuario, int idVehiculo) {
        Permisos.exigir(usuario, Accion.VER_REPORTES);
        return Transaccion.devolviendo(conexion -> {
            if (new VehiculoDaoJdbc(conexion).buscarPorId(idVehiculo).isEmpty()) {
                throw new ReglaNegocioException("El vehículo #" + idVehiculo + " no existe.");
            }
            return new ReporteDaoJdbc(conexion).historialDeVehiculo(idVehiculo);
        });
    }

    public void exportarPdf(Usuario usuario, Reporte reporte, Path destino) {
        Permisos.exigir(usuario, Accion.VER_REPORTES);
        ExportadorReportes.aPdf(reporte, destino);
    }

    public void exportarExcel(Usuario usuario, Reporte reporte, Path destino) {
        Permisos.exigir(usuario, Accion.VER_REPORTES);
        ExportadorReportes.aExcel(reporte, destino);
    }

    private static Reporte consultar(Usuario usuario, LocalDate desde, LocalDate hasta,
                                     BiFunction<LocalDateTime, LocalDateTime, Reporte> consulta) {
        Permisos.exigir(usuario, Accion.VER_REPORTES);
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        Validar.obligatorio(consulta, "consulta");
        return consulta.apply(desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay());
    }
}
