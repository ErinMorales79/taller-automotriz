package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.TecnicoDao;
import com.taller.nucleo.entidad.Tecnico;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class TecnicoDaoJdbc implements TecnicoDao {

    private static final String SELECCION = "SELECT id, nombre, especialidad, activo FROM tecnico ";

    private final Connection conexion;

    public TecnicoDaoJdbc(Connection conexion) {
        this.conexion = conexion;
    }

    private static Tecnico mapear(ResultSet fila) throws SQLException {
        Tecnico tecnico = new Tecnico(fila.getString("nombre"));
        tecnico.setEspecialidad(fila.getString("especialidad"));
        tecnico.setActivo(fila.getBoolean("activo"));
        tecnico.setId(fila.getInt("id"));
        return tecnico;
    }

    @Override
    public Tecnico insertar(Tecnico tecnico) {
        tecnico.setId(Jdbc.insertar(conexion,
                "INSERT INTO tecnico (nombre, especialidad, activo) VALUES (?, ?, ?)",
                tecnico.getNombre(), tecnico.getEspecialidad(), tecnico.isActivo()));
        return tecnico;
    }

    @Override
    public Optional<Tecnico> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE id = ?", TecnicoDaoJdbc::mapear, id);
    }

    @Override
    public List<Tecnico> listar() {
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY nombre", TecnicoDaoJdbc::mapear);
    }

    @Override
    public List<Tecnico> listarActivos() {
        return Jdbc.consultar(conexion, SELECCION + "WHERE activo = TRUE ORDER BY nombre", TecnicoDaoJdbc::mapear);
    }

    @Override
    public void actualizar(Tecnico tecnico) {
        Jdbc.ejecutarExigiendoFila(conexion,
                "UPDATE tecnico SET nombre = ?, especialidad = ?, activo = ? WHERE id = ?",
                tecnico.getNombre(), tecnico.getEspecialidad(), tecnico.isActivo(), tecnico.getId());
    }

    @Override
    public void eliminar(int id) {
        Jdbc.ejecutarExigiendoFila(conexion, "DELETE FROM tecnico WHERE id = ?", id);
    }
}
