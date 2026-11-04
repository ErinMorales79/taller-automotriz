package com.taller.api.controlador;

import com.taller.api.controlador.Dtos.RespuestaLogin;
import com.taller.api.controlador.Dtos.SolicitudLogin;
import com.taller.api.seguridad.NoAutenticadoException;
import com.taller.api.seguridad.ServicioToken;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.AccesoDenegadoException;
import com.taller.nucleo.servicio.AutenticacionServicio;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Inicio de sesión de la app móvil: valida con AutenticacionServicio y entrega un token. */
@RestController
@RequestMapping("/api/auth")
public class AutenticacionControlador {

    private final AutenticacionServicio autenticacion = new AutenticacionServicio();
    private final ServicioToken tokens;

    public AutenticacionControlador(ServicioToken tokens) {
        this.tokens = tokens;
    }

    @PostMapping("/login")
    public RespuestaLogin iniciarSesion(@RequestBody SolicitudLogin solicitud) {
        Usuario usuario;
        try {
            usuario = autenticacion.iniciarSesion(solicitud.usuario(), solicitud.contrasena());
        } catch (AccesoDenegadoException e) {
            throw new NoAutenticadoException(e.getMessage());
        }
        if (usuario.getRol() != Rol.TECNICO) {
            throw new AccesoDenegadoException("Solo los técnicos pueden usar la app móvil.");
        }
        ServicioToken.TokenEmitido emitido = tokens.emitir(usuario);
        return new RespuestaLogin(emitido.token(), emitido.expiraEn(), usuario.getNombreUsuario(),
                usuario.getTecnico() == null ? null : usuario.getTecnico().getNombre());
    }
}
