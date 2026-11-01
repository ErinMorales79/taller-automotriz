package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.AsignacionDao;
import com.taller.nucleo.entidad.Asignacion;
import com.taller.nucleo.entidad.EstadoTarea;
import com.taller.nucleo.entidad.OrdenServicio;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AsignacionDaoJdbc implements AsignacionDao {

    private static final String SELECCION = "SELECT id, id_orden, id_tecnico, fecha_asignacion, estado_tarea, "
            + "porcentaje_avance FROM asignacion ";

    private final Connection conexion;
    private final OrdenServicioDaoJdbc ordenes;
    private final TecnicoDaoJdbc tecnicos;

    public AsignacionDaoJdbc(Connection conexion) {
        this.conexion = conexion;
        this.ordenes = new OrdenServicioDaoJdbc(conexion);
        this.tecnicos = new TecnicoDaoJdbc(conexion);
    }

    private Asignacion mapear(ResultSet fila, Map<Integer, OrdenServicio> cache) throws SQLException {
        Asignacion asignacion = new Asignacion(
                ordenes.cargar(fila.getInt("id_orden"), cache),
                Jdbc.requerido(tecnicos.buscarPorId(fila.getInt("id_tecnico")), "el técnico asignado"));
        asignacion.restaurar(Jdbc.fecha(fila, "fecha_asignacion"),
                EstadoTarea.valueOf(fila.getString("estado_tarea")), fila.getInt("porcentaje_avance"));
        asignacion.setId(fila.getInt("id"));
        return asignacion;
    }

    @Override
    public Asignacion insertar(Asignacion asignacion) {
        asignacion.setId(Jdbc.insertar(conexion,
                "INSERT INTO asignacion (id_orden, id_tecnico, fecha_asignacion, estado_tarea, porcentaje_avance) "
                        + "VALUES (?, ?, ?, ?, ?)",
                asignacion.getOrden().getId(), asignacion.getTecnico().getId(), asignacion.getFechaAsignacion(),
                asignacion.getEstadoTarea(), asignacion.getPorcentajeAvance()));
        return asignacion;
    }

    @Override
    public Optional<Asignacion> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE id = ?", fila -> mapear(fila, new HashMap<>()), id);
    }

    @Override
    public List<Asignacion> listar() {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY fecha_asignacion DESC, id DESC",
                fila -> mapear(fila, cache));
    }

    @Override
    public List<Asignacion> listarPorOrden(int idOrden) {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "WHERE id_orden = ? ORDER BY fecha_asignacion, id",
                fila -> mapear(fila, cache), idOrden);
    }

    @Override
    public List<Asignacion> listarPorTecnico(int idTecnico) {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "WHERE id_tecnico = ? ORDER BY fecha_asignacion DESC, id DESC",
                fila -> mapear(fila, cache), idTecnico);
    }

    @Override
    public void actualizar(Asignacion asignacion) {
        Jdbc.ejecutarExigiendoFila(conexion,
                "UPDATE asignacion SET id_tecnico = ?, estado_tarea = ?, porcentaje_avance = ? WHERE id = ?",
                asignacion.getTecnico().getId(), asignacion.getEstadoTarea(),
                asignacion.getPorcentajeAvance(), asignacion.getId());
    }

    @Override
    public void eliminar(int id) {
        Jdbc.ejecutarExigiendoFila(conexion, "DELETE FROM asignacion WHERE id = ?", id);
    }
}
