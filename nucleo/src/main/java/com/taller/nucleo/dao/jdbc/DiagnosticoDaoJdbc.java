package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.DiagnosticoDao;
import com.taller.nucleo.entidad.Diagnostico;
import com.taller.nucleo.entidad.OrdenServicio;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DiagnosticoDaoJdbc implements DiagnosticoDao {

    private static final String SELECCION = "SELECT id, id_orden, id_tecnico, descripcion, fecha FROM diagnostico ";

    private final Connection conexion;
    private final OrdenServicioDaoJdbc ordenes;
    private final TecnicoDaoJdbc tecnicos;

    public DiagnosticoDaoJdbc(Connection conexion) {
        this.conexion = conexion;
        this.ordenes = new OrdenServicioDaoJdbc(conexion);
        this.tecnicos = new TecnicoDaoJdbc(conexion);
    }

    private Diagnostico mapear(ResultSet fila, Map<Integer, OrdenServicio> cache) throws SQLException {
        Diagnostico diagnostico = new Diagnostico(
                ordenes.cargar(fila.getInt("id_orden"), cache),
                Jdbc.requerido(tecnicos.buscarPorId(fila.getInt("id_tecnico")), "el técnico del diagnóstico"),
                fila.getString("descripcion"));
        diagnostico.setFecha(Jdbc.fecha(fila, "fecha"));
        diagnostico.setId(fila.getInt("id"));
        return diagnostico;
    }

    @Override
    public Diagnostico insertar(Diagnostico diagnostico) {
        diagnostico.setId(Jdbc.insertar(conexion,
                "INSERT INTO diagnostico (id_orden, id_tecnico, descripcion, fecha) VALUES (?, ?, ?, ?)",
                diagnostico.getOrden().getId(), diagnostico.getTecnico().getId(),
                diagnostico.getDescripcion(), diagnostico.getFecha()));
        return diagnostico;
    }

    @Override
    public Optional<Diagnostico> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE id = ?", fila -> mapear(fila, new HashMap<>()), id);
    }

    @Override
    public List<Diagnostico> listar() {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY fecha DESC, id DESC", fila -> mapear(fila, cache));
    }

    @Override
    public List<Diagnostico> listarPorOrden(int idOrden) {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "WHERE id_orden = ? ORDER BY fecha, id",
                fila -> mapear(fila, cache), idOrden);
    }

    @Override
    public void actualizar(Diagnostico diagnostico) {
        Jdbc.ejecutarExigiendoFila(conexion, "UPDATE diagnostico SET descripcion = ? WHERE id = ?",
                diagnostico.getDescripcion(), diagnostico.getId());
    }

    @Override
    public void eliminar(int id) {
        Jdbc.ejecutarExigiendoFila(conexion, "DELETE FROM diagnostico WHERE id = ?", id);
    }
}
