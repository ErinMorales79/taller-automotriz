package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.UsuarioDao;
import com.taller.nucleo.dao.jdbc.UsuarioDaoJdbc;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.seguridad.Accion;
import com.taller.nucleo.seguridad.Permisos;
import com.taller.nucleo.util.Transaccion;
import com.taller.nucleo.util.Validar;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;

/** Alta y mantenimiento de usuarios. Las contraseñas se guardan solo como hash BCrypt. */
public final class UsuarioServicio {

    private static final int LARGO_MINIMO_CONTRASENA = 8;
    // BCrypt solo usa los primeros 72 bytes; más allá se ignorarían en silencio.
    private static final int LARGO_MAXIMO_BYTES = 72;

    /** Solo un administrador crea usuarios. Un usuario TECNICO debe vincularse a un técnico. */
    public Usuario crear(Usuario administrador, String nombreUsuario, String contrasena, Rol rol, Tecnico tecnico) {
        Permisos.exigir(administrador, Accion.GESTIONAR_USUARIOS);
        Validar.obligatorio(rol, "rol");
        if (rol == Rol.TECNICO && (tecnico == null || tecnico.getId() == null)) {
            throw new ReglaNegocioException("Un usuario con rol TECNICO debe vincularse a un técnico guardado.");
        }
        Usuario usuario = new Usuario(nombreUsuario, cifrar(contrasena), rol);
        usuario.setTecnico(tecnico);
        return Transaccion.devolviendo(conexion -> {
            new UsuarioDaoJdbc(conexion).insertar(usuario);
            AuditoriaServicio.registrar(conexion, administrador, "CREAR_USUARIO", "usuario", usuario.getId());
            return usuario;
        });
    }

    /** Cada usuario cambia su propia contraseña, y debe conocer la actual. */
    public void cambiarContrasena(Usuario usuario, String contrasenaActual, String contrasenaNueva) {
        Validar.obligatorio(usuario, "usuario");
        int idUsuario = Consultas.idGuardado(usuario.getId(), "el usuario");
        String nuevoHash = cifrar(contrasenaNueva);
        Transaccion.ejecutar(conexion -> {
            UsuarioDao usuarios = new UsuarioDaoJdbc(conexion);
            Usuario guardado = usuarios.buscarPorId(idUsuario)
                    .orElseThrow(() -> new ReglaNegocioException("El usuario no existe."));
            if (contrasenaActual == null || !BCrypt.checkpw(contrasenaActual, guardado.getContrasenaHash())) {
                throw new ReglaNegocioException("La contraseña actual no es correcta.");
            }
            guardado.setContrasenaHash(nuevoHash);
            usuarios.actualizar(guardado);
            AuditoriaServicio.registrar(conexion, guardado, "CAMBIAR_CONTRASENA", "usuario", guardado.getId());
            usuario.setContrasenaHash(nuevoHash);
        });
    }

    /** Solo un administrador cambia roles, y nunca el suyo propio (evita quedarse sin administradores). */
    public Usuario cambiarRol(Usuario administrador, int idUsuario, Rol nuevoRol, Tecnico tecnico) {
        Permisos.exigir(administrador, Accion.GESTIONAR_USUARIOS);
        if (administrador.getId() != null && administrador.getId() == idUsuario) {
            throw new ReglaNegocioException("No puede cambiar su propio rol.");
        }
        return Transaccion.devolviendo(conexion -> {
            UsuarioDao usuarios = new UsuarioDaoJdbc(conexion);
            Usuario objetivo = usuarios.buscarPorId(idUsuario)
                    .orElseThrow(() -> new ReglaNegocioException("El usuario #" + idUsuario + " no existe."));
            objetivo.asignarRol(nuevoRol, tecnico);
            usuarios.actualizar(objetivo);
            AuditoriaServicio.registrar(conexion, administrador, "CAMBIAR_ROL", "usuario", idUsuario);
            return objetivo;
        });
    }

    /** Un usuario desactivado ya no puede iniciar sesión ni operar. Nadie se desactiva a sí mismo. */
    public void desactivar(Usuario administrador, int idUsuario) {
        Permisos.exigir(administrador, Accion.GESTIONAR_USUARIOS);
        if (administrador.getId() != null && administrador.getId() == idUsuario) {
            throw new ReglaNegocioException("No puede desactivar su propio usuario.");
        }
        Transaccion.ejecutar(conexion -> {
            UsuarioDao usuarios = new UsuarioDaoJdbc(conexion);
            Usuario objetivo = usuarios.buscarPorId(idUsuario)
                    .orElseThrow(() -> new ReglaNegocioException("El usuario #" + idUsuario + " no existe."));
            objetivo.setActivo(false);
            usuarios.actualizar(objetivo);
            AuditoriaServicio.registrar(conexion, administrador, "DESACTIVAR_USUARIO", "usuario", idUsuario);
        });
    }

    private static String cifrar(String contrasena) {
        if (contrasena == null || contrasena.length() < LARGO_MINIMO_CONTRASENA) {
            throw new ReglaNegocioException(
                    "La contraseña debe tener al menos " + LARGO_MINIMO_CONTRASENA + " caracteres.");
        }
        if (contrasena.getBytes(StandardCharsets.UTF_8).length > LARGO_MAXIMO_BYTES) {
            throw new ReglaNegocioException("La contraseña es demasiado larga.");
        }
        return BCrypt.hashpw(contrasena, BCrypt.gensalt());
    }
}
