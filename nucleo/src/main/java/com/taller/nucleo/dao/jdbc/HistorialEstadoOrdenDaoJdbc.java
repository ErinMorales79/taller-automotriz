package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.HistorialEstadoOrdenDao;
import com.taller.nucleo.entidad.HistorialEstadoOrden;
import com.taller.nucleo.entidad.OrdenServicio;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class HistorialEstadoOrdenDaoJdbc implements HistorialEstadoOrdenDao {

    private static final String SELECCION =
            "SELECT id, id_orden, estado_anterior, estado_nuevo, fecha, id_usuario FROM historial_estado_orden ";

    private final Connection conexion;
    private final OrdenServicioDaoJdbc ordenes;
    private final UsuarioDaoJdbc usuarios;

    public HistorialEstadoOrdenDaoJdbc(Connection conexion) {
        this.conexion = conexion;
        this.ordenes = new OrdenServicioDaoJdbc(conexion);
        this.usuarios = new UsuarioDaoJdbc(conexion);
    }

    private HistorialEstadoOrden mapear(ResultSet fila, Map<Integer, OrdenServicio> cache) throws SQLException {
        HistorialEstadoOrden registro = new HistorialEstadoOrden(
                ordenes.cargar(fila.getInt("id_orden"), cache),
                fila.getString("estado_anterior"), fila.getString("estado_nuevo"),
                Jdbc.requerido(usuarios.buscarPorId(fila.getInt("id_usuario")), "el responsable del cambio"));
        registro.setFecha(Jdbc.fecha(fila, "fecha"));
        registro.setId(fila.getInt("id"));
        return registro;
    }

    @Override
    public HistorialEstadoOrden insertar(HistorialEstadoOrden registro) {
        registro.setId(Jdbc.insertar(conexion,
                "INSERT INTO historial_estado_orden (id_orden, estado_anterior, estado_nuevo, fecha, id_usuario) "
                        + "VALUES (?, ?, ?, ?, ?)",
                registro.getOrden().getId(), registro.getEstadoAnterior(), registro.getEstadoNuevo(),
                registro.getFecha(), registro.getResponsable().getId()));
        return registro;
    }

    @Override
    public Optional<HistorialEstadoOrden> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE id = ?", fila -> mapear(fila, new HashMap<>()), id);
    }

    @Override
    public List<HistorialEstadoOrden> listar() {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY id", fila -> mapear(fila, cache));
    }

    @Override
    public List<HistorialEstadoOrden> listarPorOrden(int idOrden) {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "WHERE id_orden = ? ORDER BY fecha, id",
                fila -> mapear(fila, cache), idOrden);
    }
}
