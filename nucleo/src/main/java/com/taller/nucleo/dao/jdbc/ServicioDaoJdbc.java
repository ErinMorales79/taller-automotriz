package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.ServicioDao;
import com.taller.nucleo.entidad.Servicio;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class ServicioDaoJdbc implements ServicioDao {

    private static final String SELECCION = "SELECT id, nombre, precio_base FROM servicio ";

    private final Connection conexion;

    public ServicioDaoJdbc(Connection conexion) {
        this.conexion = conexion;
    }

    static Servicio mapear(ResultSet fila) throws SQLException {
        Servicio servicio = new Servicio(fila.getString("nombre"), fila.getBigDecimal("precio_base"));
        servicio.setId(fila.getInt("id"));
        return servicio;
    }

    @Override
    public Servicio insertar(Servicio servicio) {
        servicio.setId(Jdbc.insertar(conexion, "INSERT INTO servicio (nombre, precio_base) VALUES (?, ?)",
                servicio.getNombre(), servicio.getPrecioBase()));
        return servicio;
    }

    @Override
    public Optional<Servicio> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE id = ?", ServicioDaoJdbc::mapear, id);
    }

    @Override
    public List<Servicio> listar() {
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY nombre", ServicioDaoJdbc::mapear);
    }

    @Override
    public List<Servicio> buscar(String texto) {
        return Jdbc.consultar(conexion, SELECCION + "WHERE nombre LIKE ? ORDER BY nombre",
                ServicioDaoJdbc::mapear, Jdbc.parcial(texto));
    }

    @Override
    public void actualizar(Servicio servicio) {
        Jdbc.ejecutarExigiendoFila(conexion, "UPDATE servicio SET nombre = ?, precio_base = ? WHERE id = ?",
                servicio.getNombre(), servicio.getPrecioBase(), servicio.getId());
    }

    @Override
    public void eliminar(int id) {
        Jdbc.ejecutarExigiendoFila(conexion, "DELETE FROM servicio WHERE id = ?", id);
    }
}
