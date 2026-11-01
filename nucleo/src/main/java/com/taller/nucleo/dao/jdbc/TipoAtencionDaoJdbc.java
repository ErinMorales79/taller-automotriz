package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.TipoAtencionDao;
import com.taller.nucleo.entidad.TipoAtencion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class TipoAtencionDaoJdbc implements TipoAtencionDao {

    private final Connection conexion;

    public TipoAtencionDaoJdbc(Connection conexion) {
        this.conexion = conexion;
    }

    private static TipoAtencion mapear(ResultSet fila) throws SQLException {
        TipoAtencion tipo = new TipoAtencion(fila.getString("nombre"));
        tipo.setId(fila.getInt("id"));
        return tipo;
    }

    @Override
    public TipoAtencion insertar(TipoAtencion tipo) {
        tipo.setId(Jdbc.insertar(conexion, "INSERT INTO tipo_atencion (nombre) VALUES (?)", tipo.getNombre()));
        return tipo;
    }

    @Override
    public Optional<TipoAtencion> buscarPorId(int id) {
        return Jdbc.uno(conexion, "SELECT id, nombre FROM tipo_atencion WHERE id = ?",
                TipoAtencionDaoJdbc::mapear, id);
    }

    @Override
    public List<TipoAtencion> listar() {
        return Jdbc.consultar(conexion, "SELECT id, nombre FROM tipo_atencion ORDER BY nombre",
                TipoAtencionDaoJdbc::mapear);
    }

    @Override
    public void actualizar(TipoAtencion tipo) {
        Jdbc.ejecutarExigiendoFila(conexion, "UPDATE tipo_atencion SET nombre = ? WHERE id = ?",
                tipo.getNombre(), tipo.getId());
    }

    @Override
    public void eliminar(int id) {
        Jdbc.ejecutarExigiendoFila(conexion, "DELETE FROM tipo_atencion WHERE id = ?", id);
    }
}
