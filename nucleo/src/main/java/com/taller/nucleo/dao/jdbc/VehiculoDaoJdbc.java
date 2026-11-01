package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.VehiculoDao;
import com.taller.nucleo.entidad.Vehiculo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class VehiculoDaoJdbc implements VehiculoDao {

    private static final String SELECCION = "SELECT v.id, v.placa, v.id_cliente, v.id_modelo, v.anio, "
            + "v.color, v.kilometraje FROM vehiculo v JOIN cliente c ON c.id = v.id_cliente ";

    private final Connection conexion;
    private final ClienteDaoJdbc clientes;
    private final ModeloDaoJdbc modelos;

    public VehiculoDaoJdbc(Connection conexion) {
        this.conexion = conexion;
        this.clientes = new ClienteDaoJdbc(conexion);
        this.modelos = new ModeloDaoJdbc(conexion);
    }

    private Vehiculo mapear(ResultSet fila) throws SQLException {
        Vehiculo vehiculo = new Vehiculo(fila.getString("placa"),
                Jdbc.requerido(clientes.buscarPorId(fila.getInt("id_cliente")), "el propietario del vehículo"),
                Jdbc.requerido(modelos.buscarPorId(fila.getInt("id_modelo")), "el modelo del vehículo"),
                fila.getInt("anio"));
        vehiculo.setColor(fila.getString("color"));
        vehiculo.setKilometraje(fila.getInt("kilometraje"));
        vehiculo.setId(fila.getInt("id"));
        return vehiculo;
    }

    @Override
    public Vehiculo insertar(Vehiculo vehiculo) {
        vehiculo.setId(Jdbc.insertar(conexion,
                "INSERT INTO vehiculo (placa, id_cliente, id_modelo, anio, color, kilometraje) VALUES (?, ?, ?, ?, ?, ?)",
                vehiculo.getPlaca(), vehiculo.getPropietario().getId(), vehiculo.getModelo().getId(),
                vehiculo.getAnio(), vehiculo.getColor(), vehiculo.getKilometraje()));
        return vehiculo;
    }

    @Override
    public Optional<Vehiculo> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE v.id = ?", this::mapear, id);
    }

    @Override
    public Optional<Vehiculo> buscarPorPlaca(String placa) {
        return Jdbc.uno(conexion, SELECCION + "WHERE v.placa = ?", this::mapear,
                placa == null ? null : placa.trim().toUpperCase(java.util.Locale.ROOT));
    }

    @Override
    public List<Vehiculo> listar() {
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY v.placa", this::mapear);
    }

    @Override
    public List<Vehiculo> listarPorCliente(int idCliente) {
        return Jdbc.consultar(conexion, SELECCION + "WHERE v.id_cliente = ? ORDER BY v.placa",
                this::mapear, idCliente);
    }

    @Override
    public List<Vehiculo> buscar(String texto) {
        String patron = Jdbc.parcial(texto);
        return Jdbc.consultar(conexion,
                SELECCION + "WHERE v.placa LIKE ? OR v.color LIKE ? OR c.nombre LIKE ? ORDER BY v.placa",
                this::mapear, patron, patron, patron);
    }

    @Override
    public void actualizar(Vehiculo vehiculo) {
        Jdbc.ejecutarExigiendoFila(conexion,
                "UPDATE vehiculo SET placa = ?, id_cliente = ?, id_modelo = ?, anio = ?, color = ?, "
                        + "kilometraje = ? WHERE id = ?",
                vehiculo.getPlaca(), vehiculo.getPropietario().getId(), vehiculo.getModelo().getId(),
                vehiculo.getAnio(), vehiculo.getColor(), vehiculo.getKilometraje(), vehiculo.getId());
    }

    @Override
    public void eliminar(int id) {
        Jdbc.ejecutarExigiendoFila(conexion, "DELETE FROM vehiculo WHERE id = ?", id);
    }
}
