package com.taller.nucleo.servicio;

import com.taller.nucleo.entidad.Diagnostico;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Prioridad;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.AccesoDenegadoException;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.soporte.BaseBD;
import com.taller.nucleo.soporte.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Diagnóstico de la orden (solo en diagnóstico) y las consultas de solo lectura que usan las pantallas. */
class DiagnosticoYConsultasTest extends BaseBD {

    private final OrdenServicioServicio ordenes = new OrdenServicioServicio();
    private final DiagnosticoServicio diagnosticos = new DiagnosticoServicio();
    private final ConsultaServicio consultas = new ConsultaServicio();

    private Datos datos;
    private Usuario recepcion;
    private Tecnico tecnico;
    private OrdenServicio orden;

    @BeforeEach
    void preparar() {
        datos = new Datos(conexion());
        recepcion = datos.usuario(Rol.RECEPCIONISTA);
        tecnico = datos.tecnico();
        orden = ordenes.crear(datos.vehiculo(), datos.tipoAtencion(), Prioridad.MEDIA, recepcion, null);
    }

    @Test
    void registraElDiagnosticoMientrasLaOrdenEstaEnDiagnostico() {
        ordenes.iniciarDiagnostico(orden.getId(), recepcion);
        Diagnostico registrado = diagnosticos.registrar(orden.getId(), tecnico.getId(), "Pastillas desgastadas", recepcion);
        assertEquals("Pastillas desgastadas", registrado.getDescripcion());
        assertEquals(1, consultas.diagnosticos(orden.getId()).size());
    }

    @Test
    void bloqueaElDiagnosticoSiLaOrdenNoEstaEnDiagnostico() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> diagnosticos.registrar(orden.getId(), tecnico.getId(), "Ruido en el motor", recepcion));
        assertEquals("Solo se puede registrar un diagnóstico mientras la orden está en diagnóstico.", error.getMessage());
        assertTrue(consultas.diagnosticos(orden.getId()).isEmpty());
    }

    @Test
    void elTecnicoNoPuedeRegistrarDiagnosticosPorPermisos() {
        ordenes.iniciarDiagnostico(orden.getId(), recepcion);
        Usuario soloTecnico = datos.usuario(Rol.TECNICO);
        assertThrows(AccesoDenegadoException.class,
                () -> diagnosticos.registrar(orden.getId(), tecnico.getId(), "Ruido", soloTecnico));
    }

    @Test
    void laConsultaDeOrdenActivaAvisaAntesDeCrearUnDuplicado() {
        assertTrue(consultas.existeOrdenActiva(orden.getVehiculo().getId(), orden.getTipoAtencion().getId()));
        assertFalse(consultas.existeOrdenActiva(orden.getVehiculo().getId(), datos.tipoAtencion().getId()));
        assertTrue(consultas.ordenes("RECIBIDA", orden.getVehiculo().getPlaca()).stream()
                .anyMatch(o -> o.getId().equals(orden.getId())));
    }

    @Test
    void soloElAdministradorConsultaUsuariosYElEncargadoLasCompras() {
        assertThrows(AccesoDenegadoException.class, () -> consultas.usuarios(recepcion));
        assertThrows(AccesoDenegadoException.class, () -> consultas.compras(recepcion));
        assertFalse(consultas.usuarios(datos.usuario(Rol.ADMINISTRADOR)).isEmpty());
        consultas.compras(datos.usuario(Rol.ENCARGADO_INVENTARIO));
    }
}
