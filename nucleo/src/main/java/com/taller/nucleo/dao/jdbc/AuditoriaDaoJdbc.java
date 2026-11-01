package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.AuditoriaDao;
import com.taller.nucleo.entidad.Auditoria;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class AuditoriaDaoJdbc implements AuditoriaDao {

    private static final String SELECCION =
            "SELECT id, id_usuario, accion, tabla_afectada, id_registro, fecha FROM auditoria ";

    private final Connection conexion;
    private final UsuarioDaoJdbc usuarios;

    public AuditoriaDaoJdbc(Connection conexion) {
        this.conexion = conexion;
        this.usuarios = new UsuarioDaoJdbc(conexion);
    }

    private Auditoria mapear(ResultSet fila) throws SQLException {
        Auditoria registro = new Auditoria(
                Jdbc.requerido(usuarios.buscarPorId(fila.getInt("id_usuario")), "el usuario de la auditoría"),
                fila.getString("accion"), fila.getString("tabla_afectada"),
                Jdbc.enteroOpcional(fila, "id_registro"));
        registro.setFecha(Jdbc.fecha(fila, "fecha"));
        registro.setId(fila.getInt("id"));
        return registro;
    }

    @Override
    public Auditoria insertar(Auditoria registro) {
        registro.setId(Jdbc.insertar(conexion,
                "INSERT INTO auditoria (id_usuario, accion, tabla_afectada, id_registro, fecha) VALUES (?, ?, ?, ?, ?)",
                registro.getUsuario().getId(), registro.getAccion(), registro.getTablaAfectada(),
                registro.getIdRegistro(), registro.getFecha()));
        return registro;
    }

    @Override
    public Optional<Auditoria> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE id = ?", this::mapear, id);
    }

    @Override
    public List<Auditoria> listar() {
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY fecha DESC, id DESC", this::mapear);
    }

    @Override
    public List<Auditoria> listarPorUsuario(int idUsuario) {
        return Jdbc.consultar(conexion, SELECCION + "WHERE id_usuario = ? ORDER BY fecha DESC, id DESC",
                this::mapear, idUsuario);
    }

    @Override
    public List<Auditoria> listarPorTabla(String tablaAfectada) {
        return Jdbc.consultar(conexion, SELECCION + "WHERE tabla_afectada = ? ORDER BY fecha DESC, id DESC",
                this::mapear, tablaAfectada);
    }
}
