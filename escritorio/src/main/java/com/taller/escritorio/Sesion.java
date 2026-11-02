package com.taller.escritorio;

import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.seguridad.Accion;
import com.taller.nucleo.seguridad.Permisos;

/** Usuario que inició sesión. Las vistas lo usan solo para mostrar u ocultar botones: el permiso real lo exige el servicio. */
public final class Sesion {

    private static Usuario usuario;

    private Sesion() {
    }

    public static void iniciar(Usuario autenticado) {
        usuario = autenticado;
    }

    public static void cerrar() {
        usuario = null;
    }

    public static Usuario usuario() {
        if (usuario == null) {
            throw new IllegalStateException("No hay una sesión iniciada.");
        }
        return usuario;
    }

    public static boolean puede(Accion accion) {
        return usuario != null && Permisos.puede(usuario, accion);
    }
}
