package com.taller.api.seguridad;

import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.AccesoDenegadoException;
import com.taller.nucleo.servicio.AutenticacionServicio;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Exige un token válido en toda la API menos el inicio de sesión. Además comprueba en la base de datos
 * que el usuario siga activo y sea técnico (solo el rol TECNICO usa la API). Deja el usuario en el
 * atributo "usuario" de la petición.
 */
@Component
public class FiltroJwt extends OncePerRequestFilter {

    public static final String ATRIBUTO_USUARIO = "usuario";
    static final String RUTA_LOGIN = "/api/auth/login";
    private static final String PREFIJO = "Bearer ";

    private final ServicioToken tokens;
    private final AutenticacionServicio autenticacion = new AutenticacionServicio();

    public FiltroJwt(ServicioToken tokens) {
        this.tokens = tokens;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest peticion) {
        return RUTA_LOGIN.equals(peticion.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String cabecera = peticion.getHeader("Authorization");
        Optional<Integer> idUsuario = cabecera != null && cabecera.startsWith(PREFIJO)
                ? tokens.idUsuario(cabecera.substring(PREFIJO.length()).trim()) : Optional.empty();
        if (idUsuario.isEmpty()) {
            rechazar(respuesta, HttpServletResponse.SC_UNAUTHORIZED, "Falta el token o no es válido. Inicia sesión de nuevo.");
            return;
        }
        try {
            Usuario usuario = autenticacion.usuarioActivo(idUsuario.get());
            if (usuario.getRol() != Rol.TECNICO) {
                rechazar(respuesta, HttpServletResponse.SC_FORBIDDEN, "Solo los técnicos pueden usar la app móvil.");
                return;
            }
            peticion.setAttribute(ATRIBUTO_USUARIO, usuario);
        } catch (AccesoDenegadoException e) {
            rechazar(respuesta, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }
        cadena.doFilter(peticion, respuesta);
    }

    private static void rechazar(HttpServletResponse respuesta, int estado, String mensaje) throws IOException {
        respuesta.setStatus(estado);
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write("{\"mensaje\":\"" + mensaje.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}");
    }
}
