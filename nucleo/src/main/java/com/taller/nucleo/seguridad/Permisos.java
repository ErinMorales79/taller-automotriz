package com.taller.nucleo.seguridad;

import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.AccesoDenegadoException;
import com.taller.nucleo.util.Validar;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Matriz de permisos por rol. Los servicios la consultan antes de actuar, así que la regla
 * se cumple aunque la pantalla no oculte el botón.
 */
public final class Permisos {

    private static final Map<Rol, Set<Accion>> POR_ROL = new EnumMap<>(Rol.class);

    static {
        POR_ROL.put(Rol.ADMINISTRADOR, EnumSet.allOf(Accion.class));
        POR_ROL.put(Rol.RECEPCIONISTA, EnumSet.of(Accion.GESTIONAR_CLIENTES_Y_VEHICULOS, Accion.CREAR_ORDEN,
                Accion.GESTIONAR_ORDEN, Accion.GESTIONAR_PRESUPUESTO, Accion.ASIGNAR_TECNICOS,
                Accion.REGISTRAR_PAGO, Accion.VER_REPORTES));
        POR_ROL.put(Rol.TECNICO, EnumSet.of(Accion.ACTUALIZAR_AVANCE, Accion.REGISTRAR_CONSUMO));
        POR_ROL.put(Rol.ENCARGADO_INVENTARIO, EnumSet.of(Accion.GESTIONAR_INVENTARIO,
                Accion.REGISTRAR_CONSUMO, Accion.VER_REPORTES));
    }

    private Permisos() {
    }

    /** Un usuario desactivado no puede hacer nada, tenga el rol que tenga. */
    public static boolean puede(Usuario usuario, Accion accion) {
        return usuario != null && usuario.isActivo() && POR_ROL.get(usuario.getRol()).contains(accion);
    }

    public static void exigir(Usuario usuario, Accion accion) {
        Validar.obligatorio(usuario, "usuario");
        if (!usuario.isActivo()) {
            throw new AccesoDenegadoException("El usuario " + usuario.getNombreUsuario() + " está desactivado.");
        }
        if (!puede(usuario, accion)) {
            throw new AccesoDenegadoException("El rol " + usuario.getRol() + " no tiene permiso para "
                    + accion.getDescripcion() + ".");
        }
    }
}
