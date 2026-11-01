package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.ObservacionOrdenDao;
import com.taller.nucleo.entidad.ObservacionOrden;
import com.taller.nucleo.entidad.OrdenServicio;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ObservacionOrdenDaoJdbc implements ObservacionOrdenDao {

    private static final String SELECCION =
            "SELECT id, id_orden, id_tecnico, texto, fecha FROM observacion_orden ";

    private final Connection conexion;
    private final OrdenServicioDaoJdbc ordenes;
    private final TecnicoDaoJdbc tecnicos;

    public ObservacionOrdenDaoJdbc(Connection conexion) {
        this.conexion = conexion;
        this.ordenes = new OrdenServicioDaoJdbc(conexion);
        this.tecnicos = new TecnicoDaoJdbc(conexion);
    }

    private ObservacionOrden mapear(ResultSet fila, Map<Integer, OrdenServicio> cache) throws SQLException {
        ObservacionOrden observacion = new ObservacionOrden(
                ordenes.cargar(fila.getInt("id_orden"), cache),
                Jdbc.requerido(tecnicos.buscarPorId(fila.getInt("id_tecnico")), "el técnico de la observación"),
                fila.getString("texto"));
        observacion.setFecha(Jdbc.fecha(fila, "fecha"));
        observacion.setId(fila.getInt("id"));
        return observacion;
    }

    @Override
    public ObservacionOrden insertar(ObservacionOrden observacion) {
        observacion.setId(Jdbc.insertar(conexion,
                "INSERT INTO observacion_orden (id_orden, id_tecnico, texto, fecha) VALUES (?, ?, ?, ?)",
                observacion.getOrden().getId(), observacion.getTecnico().getId(),
                observacion.getTexto(), observacion.getFecha()));
        return observacion;
    }

    @Override
    public Optional<ObservacionOrden> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE id = ?", fila -> mapear(fila, new HashMap<>()), id);
    }

    @Override
    public List<ObservacionOrden> listar() {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY fecha DESC, id DESC", fila -> mapear(fila, cache));
    }

    @Override
    public List<ObservacionOrden> listarPorOrden(int idOrden) {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "WHERE id_orden = ? ORDER BY fecha, id",
                fila -> mapear(fila, cache), idOrden);
    }
}
