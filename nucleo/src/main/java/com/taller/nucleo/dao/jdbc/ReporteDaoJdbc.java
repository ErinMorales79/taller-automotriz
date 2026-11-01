package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.ReporteDao;
import com.taller.nucleo.reporte.Reporte;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ReporteDaoJdbc implements ReporteDao {

    // Una orden entregada envejece hasta su entrega; una cancelada, hasta su último cambio; el resto, hasta hoy.
    private static final String FIN_DE_LA_ORDEN = "COALESCE(o.fecha_entrega, CASE WHEN o.estado = 'CANCELADA' "
            + "THEN (SELECT MAX(h.fecha) FROM historial_estado_orden h WHERE h.id_orden = o.id) END, NOW())";

    private final Connection conexion;

    public ReporteDaoJdbc(Connection conexion) {
        this.conexion = conexion;
    }

    @Override
    public Reporte ordenesPorEstado(LocalDateTime desde, LocalDateTime hastaExclusivo) {
        String dias = "TIMESTAMPDIFF(DAY, o.fecha_ingreso, " + FIN_DE_LA_ORDEN + ")";
        return tabla("Órdenes por estado y antigüedad",
                List.of("Estado", "Órdenes", "Antigüedad promedio (días)", "Antigüedad máxima (días)"),
                "SELECT o.estado, COUNT(*), ROUND(AVG(" + dias + "), 1), MAX(" + dias + ") "
                        + "FROM orden_servicio o "
                        + "WHERE (? IS NULL OR o.fecha_ingreso >= ?) AND (? IS NULL OR o.fecha_ingreso < ?) "
                        + "GROUP BY o.estado ORDER BY FIELD(o.estado, 'RECIBIDA', 'EN_DIAGNOSTICO', "
                        + "'ESPERANDO_APROBACION', 'EN_REPARACION', 'FINALIZADA', 'ENTREGADA', 'CANCELADA')",
                desde, desde, hastaExclusivo, hastaExclusivo);
    }

    @Override
    public Reporte productividadPorTecnico(LocalDateTime desde, LocalDateTime hastaExclusivo) {
        return tabla("Productividad por técnico",
                List.of("Técnico", "Tareas", "Terminadas", "Órdenes", "Avance promedio (%)"),
                "SELECT t.nombre, COUNT(a.id), COALESCE(SUM(a.estado_tarea = 'TERMINADA'), 0), "
                        + "COUNT(DISTINCT a.id_orden), COALESCE(ROUND(AVG(a.porcentaje_avance), 1), 0) "
                        + "FROM tecnico t LEFT JOIN asignacion a ON a.id_tecnico = t.id "
                        + "AND (? IS NULL OR a.fecha_asignacion >= ?) AND (? IS NULL OR a.fecha_asignacion < ?) "
                        + "GROUP BY t.id, t.nombre ORDER BY 3 DESC, 2 DESC, t.nombre",
                desde, desde, hastaExclusivo, hastaExclusivo);
    }

    @Override
    public Reporte repuestosMasConsumidos(LocalDateTime desde, LocalDateTime hastaExclusivo, int limite) {
        return tabla("Repuestos de mayor consumo",
                List.of("Código", "Repuesto", "Unidades consumidas", "Monto"),
                "SELECT r.codigo, r.nombre, SUM(c.cantidad), SUM(c.cantidad * c.precio_unitario) "
                        + "FROM consumo_repuesto c JOIN repuesto r ON r.id = c.id_repuesto "
                        + "WHERE (? IS NULL OR c.fecha >= ?) AND (? IS NULL OR c.fecha < ?) "
                        + "GROUP BY r.id, r.codigo, r.nombre ORDER BY 3 DESC, r.codigo LIMIT ?",
                desde, desde, hastaExclusivo, hastaExclusivo, limite);
    }

    @Override
    public Reporte ingresosPorServicio(LocalDateTime desde, LocalDateTime hastaExclusivo) {
        return tabla("Ingresos por servicio",
                List.of("Servicio", "Veces facturado", "Horas", "Ingresos"),
                "SELECT s.nombre, COUNT(d.id), SUM(d.cantidad_horas), SUM(d.cantidad_horas * d.precio_unitario) "
                        + "FROM detalle_servicio d "
                        + "JOIN presupuesto p ON p.id = d.id_presupuesto AND p.estado = 'APROBADO' "
                        + "JOIN orden_servicio o ON o.id = p.id_orden AND o.estado IN ('FINALIZADA', 'ENTREGADA') "
                        + "JOIN servicio s ON s.id = d.id_servicio "
                        + "WHERE (? IS NULL OR o.fecha_ingreso >= ?) AND (? IS NULL OR o.fecha_ingreso < ?) "
                        + "GROUP BY s.id, s.nombre ORDER BY 4 DESC, s.nombre",
                desde, desde, hastaExclusivo, hastaExclusivo);
    }

    @Override
    public Reporte historialDeVehiculo(int idVehiculo) {
        return tabla("Historial de mantenimiento del vehículo",
                List.of("Orden", "Ingreso", "Tipo de atención", "Prioridad", "Estado", "Entrega",
                        "Total aprobado", "Último diagnóstico"),
                "SELECT o.id, o.fecha_ingreso, ta.nombre, o.prioridad, o.estado, o.fecha_entrega, "
                        + "COALESCE((SELECT p.total FROM presupuesto p WHERE p.id_orden = o.id "
                        + "AND p.estado = 'APROBADO' ORDER BY p.id DESC LIMIT 1), 0), "
                        + "(SELECT d.descripcion FROM diagnostico d WHERE d.id_orden = o.id ORDER BY d.id DESC LIMIT 1) "
                        + "FROM orden_servicio o JOIN tipo_atencion ta ON ta.id = o.id_tipo_atencion "
                        + "WHERE o.id_vehiculo = ? ORDER BY o.fecha_ingreso DESC, o.id DESC",
                idVehiculo);
    }

    private Reporte tabla(String titulo, List<String> columnas, String sql, Object... parametros) {
        int ancho = columnas.size();
        List<List<Object>> filas = Jdbc.consultar(conexion, sql, fila -> leerFila(fila, ancho), parametros);
        return new Reporte(titulo, columnas, filas);
    }

    private static List<Object> leerFila(java.sql.ResultSet fila, int ancho) throws SQLException {
        List<Object> valores = new ArrayList<>(ancho);
        for (int i = 1; i <= ancho; i++) {
            valores.add(fila.getObject(i));
        }
        return valores;
    }
}
