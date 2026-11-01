package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.ProveedorDao;
import com.taller.nucleo.entidad.Proveedor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class ProveedorDaoJdbc implements ProveedorDao {

    private final Connection conexion;

    public ProveedorDaoJdbc(Connection conexion) {
        this.conexion = conexion;
    }

    static Proveedor mapear(ResultSet fila) throws SQLException {
        Proveedor proveedor = new Proveedor(fila.getString("nombre"));
        proveedor.setTelefono(fila.getString("telefono"));
        proveedor.setId(fila.getInt("id"));
        return proveedor;
    }

    @Override
    public Proveedor insertar(Proveedor proveedor) {
        proveedor.setId(Jdbc.insertar(conexion, "INSERT INTO proveedor (nombre, telefono) VALUES (?, ?)",
                proveedor.getNombre(), proveedor.getTelefono()));
        return proveedor;
    }

    @Override
    public Optional<Proveedor> buscarPorId(int id) {
        return Jdbc.uno(conexion, "SELECT id, nombre, telefono FROM proveedor WHERE id = ?",
                ProveedorDaoJdbc::mapear, id);
    }

    @Override
    public List<Proveedor> listar() {
        return Jdbc.consultar(conexion, "SELECT id, nombre, telefono FROM proveedor ORDER BY nombre",
                ProveedorDaoJdbc::mapear);
    }

    @Override
    public void actualizar(Proveedor proveedor) {
        Jdbc.ejecutarExigiendoFila(conexion, "UPDATE proveedor SET nombre = ?, telefono = ? WHERE id = ?",
                proveedor.getNombre(), proveedor.getTelefono(), proveedor.getId());
    }

    @Override
    public void eliminar(int id) {
        Jdbc.ejecutarExigiendoFila(conexion, "DELETE FROM proveedor WHERE id = ?", id);
    }
}
