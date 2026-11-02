package com.taller.escritorio.controlador;

import com.taller.escritorio.Servicios;
import com.taller.escritorio.Sesion;
import com.taller.escritorio.util.Tarea;
import com.taller.escritorio.vista.VentanaLogin;
import com.taller.escritorio.vista.VentanaPrincipal;
import com.taller.nucleo.excepcion.ReglaNegocioException;

/** Valida las credenciales con AutenticacionServicio y abre la ventana principal. */
public final class ControladorLogin {

    private final VentanaLogin vista;

    public ControladorLogin(VentanaLogin vista) {
        this.vista = vista;
    }

    public void iniciar(String nombreUsuario, String contrasena) {
        if (nombreUsuario.isBlank() || contrasena.isEmpty()) {
            vista.mostrarError("Escribe tu usuario y tu contraseña.");
            return;
        }
        try {
            Tarea.ejecutar(vista.getRootPane(), () -> {
                try {
                    return Servicios.AUTENTICACION.iniciarSesion(nombreUsuario, contrasena);
                } catch (ReglaNegocioException e) {
                    return e;
                }
            }, resultado -> {
                if (resultado instanceof ReglaNegocioException error) {
                    vista.mostrarError(error.getMessage());
                } else {
                    Sesion.iniciar((com.taller.nucleo.entidad.Usuario) resultado);
                    vista.dispose();
                    VentanaPrincipal principal = new VentanaPrincipal();
                    principal.setControlador(new ControladorPrincipal(principal));
                    principal.setVisible(true);
                }
            });
        } catch (RuntimeException e) {
            vista.mostrarError(e.getMessage());
        }
    }
}
