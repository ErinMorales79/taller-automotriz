package com.taller.escritorio.controlador;

import com.taller.escritorio.Servicios;
import com.taller.escritorio.Sesion;
import com.taller.escritorio.util.Tarea;
import com.taller.escritorio.vista.PanelUsuarios;
import com.taller.escritorio.vista.componentes.Mensajes;
import com.taller.escritorio.vista.editor.EditorUsuario;
import com.taller.nucleo.entidad.Usuario;

/** Administración de usuarios: alta, cambio de rol y desactivación (solo el administrador). */
public final class ControladorUsuarios {

    private final PanelUsuarios vista;

    public ControladorUsuarios(PanelUsuarios vista) {
        this.vista = vista;
    }

    public void cargar() {
        Tarea.ejecutar(vista, () -> Servicios.CONSULTAS.usuarios(Sesion.usuario()), vista::mostrar);
    }

    public void nuevo() {
        Formularios.pedir(vista.ventana(), "Nuevo usuario", "Crear usuario", new EditorUsuario(false),
                datos -> Servicios.USUARIOS.crear(Sesion.usuario(), datos.nombreUsuario(), datos.contrasena(),
                        datos.rol(), datos.tecnico()), this::cargar);
    }

    public void cambiarRol(Usuario usuario) {
        if (usuario == null) {
            return;
        }
        Formularios.pedir(vista.ventana(), "Cambiar rol de " + usuario.getNombreUsuario(), "Cambiar rol",
                new EditorUsuario(true), datos -> Servicios.USUARIOS.cambiarRol(Sesion.usuario(), usuario.getId(),
                        datos.rol(), datos.tecnico()), this::cargar);
    }

    public void desactivar(Usuario usuario) {
        if (usuario != null && Mensajes.confirmar(vista, "¿Desactivar al usuario \"" + usuario.getNombreUsuario()
                + "\"? Ya no podrá iniciar sesión.")) {
            Tarea.ejecutar(vista, () -> Servicios.USUARIOS.desactivar(Sesion.usuario(), usuario.getId()), this::cargar);
        }
    }
}
