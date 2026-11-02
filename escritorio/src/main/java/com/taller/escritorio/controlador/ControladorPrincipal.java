package com.taller.escritorio.controlador;

import com.taller.escritorio.Servicios;
import com.taller.escritorio.Sesion;
import com.taller.escritorio.vista.VentanaLogin;
import com.taller.escritorio.vista.VentanaPrincipal;
import com.taller.escritorio.vista.componentes.Mensajes;
import com.taller.escritorio.vista.editor.EditorContrasena;

/** Cierra la sesión y vuelve a la pantalla de acceso. */
public final class ControladorPrincipal {

    private final VentanaPrincipal vista;

    public ControladorPrincipal(VentanaPrincipal vista) {
        this.vista = vista;
    }

    public void cambiarContrasena() {
        Formularios.pedir(vista, "Cambiar mi contraseña", "Cambiar contraseña", new EditorContrasena(),
                datos -> Servicios.USUARIOS.cambiarContrasena(Sesion.usuario(), datos.actual(), datos.nueva()),
                () -> Mensajes.informar(vista, "Tu contraseña se cambió correctamente."));
    }

    public void cerrarSesion() {
        if (!Mensajes.confirmar(vista, "¿Cerrar la sesión de " + Sesion.usuario().getNombreUsuario() + "?")) {
            return;
        }
        Sesion.cerrar();
        vista.dispose();
        VentanaLogin login = new VentanaLogin();
        login.setControlador(new ControladorLogin(login));
        login.setVisible(true);
    }
}
