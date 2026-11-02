package com.taller.escritorio.controlador;

import com.taller.escritorio.Servicios;
import com.taller.escritorio.Sesion;
import com.taller.escritorio.vista.DialogoAutorizacion;
import com.taller.escritorio.vista.componentes.Mensajes;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.ReglaNegocioException;

import java.awt.Component;

/**
 * Obtiene al administrador que autoriza una excepción (reglas 1 y 4). Si quien trabaja ya es
 * administrador basta con que confirme; si no, se piden las credenciales de uno.
 */
public final class ControladorAutorizacion {

    private ControladorAutorizacion() {
    }

    /** Devuelve el administrador que autoriza, o null si se cancela o las credenciales no sirven. */
    public static Usuario administrador(Component padre, String motivo) {
        Usuario actual = Sesion.usuario();
        if (actual.puedeAutorizarExcepciones()) {
            return Mensajes.confirmar(padre, motivo + "\n¿Autorizas esta excepción como " + actual.getNombreUsuario() + "?")
                    ? actual : null;
        }
        return DialogoAutorizacion.pedir(padre, motivo).map(credenciales -> {
            try {
                Usuario candidato = Servicios.AUTENTICACION.iniciarSesion(credenciales.usuario(), credenciales.contrasena());
                if (!candidato.puedeAutorizarExcepciones()) {
                    Mensajes.avisar(padre, "Ese usuario no es administrador y no puede autorizar excepciones.");
                    return null;
                }
                return candidato;
            } catch (ReglaNegocioException e) {
                Mensajes.avisar(padre, e.getMessage());
                return null;
            }
        }).orElse(null);
    }
}
