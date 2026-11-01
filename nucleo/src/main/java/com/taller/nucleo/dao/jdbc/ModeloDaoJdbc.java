package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.ModeloDao;
import com.taller.nucleo.entidad.Marca;
import com.taller.nucleo.entidad.Modelo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class ModeloDaoJdbc implements ModeloDao {

    private static final String SELECCION = "SELECT mo.id, mo.nombre, ma.id AS id_marca, ma.nombre AS nombre_marca "
            + "FROM modelo mo JOIN marca ma ON ma.id = mo.id_marca ";

    private final Connection conexion;

    public ModeloDaoJdbc(Connection conexion) {
        this.conexion = conexion;
    }

    private static Modelo mapear(ResultSet fila) throws SQLException {
        Marca marca = new Marca(fila.getString("nombre_marca"));
        marca.setId(fila.getInt("id_marca"));
        Modelo modelo = new Modelo(marca, fila.getString("nombre"));
        modelo.setId(fila.getInt("id"));
        return modelo;
    }

    @Override
    public Modelo insertar(Modelo modelo) {
        modelo.setId(Jdbc.insertar(conexion, "INSERT INTO modelo (id_marca, nombre) VALUES (?, ?)",
                modelo.getMarca().getId(), modelo.getNombre()));
        return modelo;
    }

    @Override
    public Optional<Modelo> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE mo.id = ?", ModeloDaoJdbc::mapear, id);
    }

    @Override
    public List<Modelo> listar() {
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY ma.nombre, mo.nombre", ModeloDaoJdbc::mapear);
    }

    @Override
    public List<Modelo> listarPorMarca(int idMarca) {
        return Jdbc.consultar(conexion, SELECCION + "WHERE ma.id = ? ORDER BY mo.nombre",
                ModeloDaoJdbc::mapear, idMarca);
    }

    @Override
    public void actualizar(Modelo modelo) {
        Jdbc.ejecutarExigiendoFila(conexion, "UPDATE modelo SET id_marca = ?, nombre = ? WHERE id = ?",
                modelo.getMarca().getId(), modelo.getNombre(), modelo.getId());
    }

    @Override
    public void eliminar(int id) {
        Jdbc.ejecutarExigiendoFila(conexion, "DELETE FROM modelo WHERE id = ?", id);
    }
}
