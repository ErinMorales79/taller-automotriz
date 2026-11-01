package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.ClienteDao;
import com.taller.nucleo.entidad.Cliente;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class ClienteDaoJdbc implements ClienteDao {

    private static final String SELECCION =
            "SELECT id, documento, nombre, telefono, correo, direccion FROM cliente ";

    private final Connection conexion;

    public ClienteDaoJdbc(Connection conexion) {
        this.conexion = conexion;
    }

    private static Cliente mapear(ResultSet fila) throws SQLException {
        Cliente cliente = new Cliente(fila.getString("documento"), fila.getString("nombre"));
        cliente.setTelefono(fila.getString("telefono"));
        cliente.setCorreo(fila.getString("correo"));
        cliente.setDireccion(fila.getString("direccion"));
        cliente.setId(fila.getInt("id"));
        return cliente;
    }

    @Override
    public Cliente insertar(Cliente cliente) {
        cliente.setId(Jdbc.insertar(conexion,
                "INSERT INTO cliente (documento, nombre, telefono, correo, direccion) VALUES (?, ?, ?, ?, ?)",
                cliente.getDocumento(), cliente.getNombre(), cliente.getTelefono(),
                cliente.getCorreo(), cliente.getDireccion()));
        return cliente;
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE id = ?", ClienteDaoJdbc::mapear, id);
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documento) {
        return Jdbc.uno(conexion, SELECCION + "WHERE documento = ?", ClienteDaoJdbc::mapear, documento);
    }

    @Override
    public List<Cliente> listar() {
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY nombre", ClienteDaoJdbc::mapear);
    }

    @Override
    public List<Cliente> buscar(String texto) {
        String patron = Jdbc.parcial(texto);
        return Jdbc.consultar(conexion,
                SELECCION + "WHERE documento LIKE ? OR nombre LIKE ? OR telefono LIKE ? ORDER BY nombre",
                ClienteDaoJdbc::mapear, patron, patron, patron);
    }

    @Override
    public void actualizar(Cliente cliente) {
        Jdbc.ejecutarExigiendoFila(conexion,
                "UPDATE cliente SET documento = ?, nombre = ?, telefono = ?, correo = ?, direccion = ? WHERE id = ?",
                cliente.getDocumento(), cliente.getNombre(), cliente.getTelefono(),
                cliente.getCorreo(), cliente.getDireccion(), cliente.getId());
    }

    @Override
    public void eliminar(int id) {
        Jdbc.ejecutarExigiendoFila(conexion, "DELETE FROM cliente WHERE id = ?", id);
    }
}
