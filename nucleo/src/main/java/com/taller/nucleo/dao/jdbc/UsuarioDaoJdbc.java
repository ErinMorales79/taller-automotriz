package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.UsuarioDao;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class UsuarioDaoJdbc implements UsuarioDao {

    private static final String SELECCION =
            "SELECT id, nombre_usuario, contrasena_hash, rol, activo, id_tecnico FROM usuario ";

    private final Connection conexion;
    private final TecnicoDaoJdbc tecnicos;

    public UsuarioDaoJdbc(Connection conexion) {
        this.conexion = conexion;
        this.tecnicos = new TecnicoDaoJdbc(conexion);
    }

    private Usuario mapear(ResultSet fila) throws SQLException {
        Usuario usuario = new Usuario(fila.getString("nombre_usuario"), fila.getString("contrasena_hash"),
                Rol.valueOf(fila.getString("rol")));
        usuario.setActivo(fila.getBoolean("activo"));
        usuario.setId(fila.getInt("id"));
        Integer idTecnico = Jdbc.enteroOpcional(fila, "id_tecnico");
        if (idTecnico != null) {
            usuario.setTecnico(Jdbc.requerido(tecnicos.buscarPorId(idTecnico), "el técnico del usuario"));
        }
        return usuario;
    }

    private static Integer idTecnico(Usuario usuario) {
        return usuario.getTecnico() == null ? null : usuario.getTecnico().getId();
    }

    @Override
    public Usuario insertar(Usuario usuario) {
        usuario.setId(Jdbc.insertar(conexion,
                "INSERT INTO usuario (nombre_usuario, contrasena_hash, rol, activo, id_tecnico) VALUES (?, ?, ?, ?, ?)",
                usuario.getNombreUsuario(), usuario.getContrasenaHash(), usuario.getRol(),
                usuario.isActivo(), idTecnico(usuario)));
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE id = ?", this::mapear, id);
    }

    @Override
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        return Jdbc.uno(conexion, SELECCION + "WHERE nombre_usuario = ?", this::mapear, nombreUsuario);
    }

    @Override
    public List<Usuario> listar() {
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY nombre_usuario", this::mapear);
    }

    @Override
    public void actualizar(Usuario usuario) {
        Jdbc.ejecutarExigiendoFila(conexion,
                "UPDATE usuario SET nombre_usuario = ?, contrasena_hash = ?, rol = ?, activo = ?, id_tecnico = ? "
                        + "WHERE id = ?",
                usuario.getNombreUsuario(), usuario.getContrasenaHash(), usuario.getRol(),
                usuario.isActivo(), idTecnico(usuario), usuario.getId());
    }

    @Override
    public void eliminar(int id) {
        Jdbc.ejecutarExigiendoFila(conexion, "DELETE FROM usuario WHERE id = ?", id);
    }
}
