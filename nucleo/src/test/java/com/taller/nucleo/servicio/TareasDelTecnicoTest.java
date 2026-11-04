package com.taller.nucleo.servicio;

import com.taller.nucleo.entidad.Asignacion;
import com.taller.nucleo.entidad.ObservacionOrden;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.AccesoDenegadoException;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.soporte.BaseBD;
import com.taller.nucleo.soporte.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.taller.nucleo.soporte.Datos.unico;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Lo que usa la app móvil: tareas propias, orden asignada y observaciones (solo del técnico asignado). */
class TareasDelTecnicoTest extends BaseBD {

    private static final String CONTRASENA = "Clave12345";

    private final TareaServicio tareas = new TareaServicio();
    private final OrdenServicioServicio ordenes = new OrdenServicioServicio();
    private final ConsultaServicio consultas = new ConsultaServicio();

    private Datos datos;
    private Usuario recepcion;
    private Usuario admin;
    private Usuario usuarioTecnico;
    private Tecnico tecnico;
    private OrdenServicio orden;

    @BeforeEach
    void preparar() {
        datos = new Datos(conexion());
        recepcion = datos.usuario(Rol.RECEPCIONISTA);
        admin = datos.usuario(Rol.ADMINISTRADOR);
        tecnico = datos.tecnico();
        usuarioTecnico = new UsuarioServicio().crear(admin, unico("tec"), CONTRASENA, Rol.TECNICO, tecnico);
        orden = datos.orden(recepcion);
        tareas.asignar(orden.getId(), tecnico.getId(), recepcion);
    }

    @Test
    void elTecnicoVeSoloSusTareas() {
        OrdenServicio ajena = datos.orden(recepcion);
        tareas.asignar(ajena.getId(), datos.tecnico().getId(), recepcion);

        List<Asignacion> propias = tareas.asignacionesDelTecnico(usuarioTecnico);

        assertEquals(1, propias.size());
        assertEquals(orden.getId(), propias.get(0).getOrden().getId());
    }

    @Test
    void elTecnicoAsignadoAgregaObservacionesYQuedanRegistradas() {
        ObservacionOrden observacion = tareas.agregarObservacion(orden.getId(), "Se cambió el filtro", usuarioTecnico);

        assertEquals("Se cambió el filtro", observacion.getTexto());
        assertEquals(1, consultas.observaciones(orden.getId()).size());
    }

    @Test
    void unTecnicoNoAsignadoNoVeNiComentaLaOrden() {
        Usuario otro = new UsuarioServicio().crear(admin, unico("tec"), CONTRASENA, Rol.TECNICO, datos.tecnico());

        assertThrows(AccesoDenegadoException.class, () -> tareas.ordenAsignada(orden.getId(), otro));
        assertThrows(AccesoDenegadoException.class, () -> tareas.agregarObservacion(orden.getId(), "Hola", otro));
        assertTrue(consultas.observaciones(orden.getId()).isEmpty());
    }

    @Test
    void noSeAgreganObservacionesAUnaOrdenCerrada() {
        ordenes.cancelar(orden.getId(), recepcion);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> tareas.agregarObservacion(orden.getId(), "Tarde", usuarioTecnico));
        assertEquals("No se pueden agregar observaciones a una orden cerrada.", error.getMessage());
    }

    @Test
    void laObservacionVaciaSeRechazaYSoloLosTecnicosUsanEstasOperaciones() {
        assertThrows(ReglaNegocioException.class, () -> tareas.agregarObservacion(orden.getId(), "  ", usuarioTecnico));
        assertThrows(AccesoDenegadoException.class, () -> tareas.asignacionesDelTecnico(recepcion));
        assertThrows(AccesoDenegadoException.class, () -> tareas.asignacionesDelTecnico(admin));
    }

    @Test
    void laSesionSoloEsValidaMientrasElUsuarioExistaYEsteActivo() {
        AutenticacionServicio autenticacion = new AutenticacionServicio();
        assertEquals(usuarioTecnico.getId(), autenticacion.usuarioActivo(usuarioTecnico.getId()).getId());

        new UsuarioServicio().desactivar(admin, usuarioTecnico.getId());

        assertThrows(AccesoDenegadoException.class, () -> autenticacion.usuarioActivo(usuarioTecnico.getId()));
        assertThrows(AccesoDenegadoException.class, () -> autenticacion.usuarioActivo(999_999));
    }
}
