package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.jdbc.UsuarioDaoJdbc;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.AccesoDenegadoException;
import com.taller.nucleo.util.Transaccion;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

/**
 * Inicio de sesión con usuario y contraseña (hash BCrypt). El mensaje de error es siempre el
 * mismo para no revelar si el usuario existe, y cada intento queda en la auditoría.
 */
public final class AutenticacionServicio {

    private static final String MENSAJE_INVALIDO = "Usuario o contraseña incorrectos.";
    // Hash de relleno: se compara igual cuando el usuario no existe, para que tarde lo mismo.
    private static final String HASH_RELLENO = BCrypt.hashpw("relleno-sin-uso", BCrypt.gensalt());

    public Usuario iniciarSesion(String nombreUsuario, String contrasena) {
        // La auditoría se confirma aunque el intento falle, por eso el error se lanza afuera.
        Optional<Usuario> autenticado = Transaccion.devolviendo(conexion -> {
            Optional<Usuario> encontrado = nombreUsuario == null ? Optional.empty()
                    : new UsuarioDaoJdbc(conexion).buscarPorNombreUsuario(nombreUsuario.trim());
            boolean correcto = coincide(contrasena, encontrado.map(Usuario::getContrasenaHash).orElse(HASH_RELLENO))
                    && encontrado.isPresent() && encontrado.get().isActivo();
            encontrado.ifPresent(usuario -> AuditoriaServicio.registrar(conexion, usuario,
                    correcto ? "LOGIN" : "LOGIN_FALLIDO", "usuario", usuario.getId()));
            return correcto ? encontrado : Optional.empty();
        });
        return autenticado.orElseThrow(() -> new AccesoDenegadoException(MENSAJE_INVALIDO));
    }

    private static boolean coincide(String contrasena, String hash) {
        if (contrasena == null || contrasena.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(contrasena, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
