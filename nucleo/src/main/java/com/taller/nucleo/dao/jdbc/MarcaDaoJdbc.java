package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.MarcaDao;
import com.taller.nucleo.entidad.Marca;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class MarcaDaoJdbc implements MarcaDao {

    private final Connection conexion;

    public MarcaDaoJdbc(Connection conexion) {
        this.conexion = conexion;
    }

    private static Marca mapear(ResultSet fila) throws SQLException {
        Marca marca = new Marca(fila.getString("nombre"));
        marca.setId(fila.getInt("id"));
        return marca;
    }

    @Override
    public Marca insertar(Marca marca) {
        marca.setId(Jdbc.insertar(conexion, "INSERT INTO marca (nombre) VALUES (?)", marca.getNombre()));
        return marca;
    }

    @Override
    public Optional<Marca> buscarPorId(int id) {
        return Jdbc.uno(conexion, "SELECT id, nombre FROM marca WHERE id = ?", MarcaDaoJdbc::mapear, id);
    }

    @Override
    public Optional<Marca> buscarPorNombre(String nombre) {
        return Jdbc.uno(conexion, "SELECT id, nombre FROM marca WHERE nombre = ?", MarcaDaoJdbc::mapear, nombre);
    }

    @Override
    public List<Marca> listar() {
        return Jdbc.consultar(conexion, "SELECT id, nombre FROM marca ORDER BY nombre", MarcaDaoJdbc::mapear);
    }

    @Override
    public void actualizar(Marca marca) {
        Jdbc.ejecutarExigiendoFila(conexion, "UPDATE marca SET nombre = ? WHERE id = ?",
                marca.getNombre(), marca.getId());
    }

    @Override
    public void eliminar(int id) {
        Jdbc.ejecutarExigiendoFila(conexion, "DELETE FROM marca WHERE id = ?", id);
    }
}
