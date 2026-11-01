package com.taller.nucleo.estado;

import com.taller.nucleo.entidad.Cliente;
import com.taller.nucleo.entidad.Marca;
import com.taller.nucleo.entidad.Modelo;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Prioridad;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.TipoAtencion;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.entidad.Vehiculo;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoOrdenTest {

    private static final List<Function<EstadoOrden, EstadoOrden>> TRANSICIONES = List.of(
            EstadoOrden::iniciarDiagnostico,
            EstadoOrden::enviarAAprobacion,
            EstadoOrden::volverADiagnostico,
            EstadoOrden::iniciarReparacion,
            EstadoOrden::finalizar,
            EstadoOrden::entregar,
            EstadoOrden::cancelar);

    private OrdenServicio ordenNueva() {
        Vehiculo vehiculo = new Vehiculo("ABC123", new Cliente("1", "Ana"),
                new Modelo(new Marca("Toyota"), "Corolla"), 2018);
        return new OrdenServicio(vehiculo, new TipoAtencion("Mantenimiento"), Prioridad.MEDIA,
                new Usuario("rosa", "hash", Rol.RECEPCIONISTA));
    }

    private long contarPermitidas(EstadoOrden estado) {
        return TRANSICIONES.stream().filter(t -> {
            try {
                t.apply(estado);
                return true;
            } catch (ReglaNegocioException e) {
                return false;
            }
        }).count();
    }

    @Test
    void ordenNuevaEmpiezaRecibidaYActiva() {
        OrdenServicio orden = ordenNueva();
        assertEquals("RECIBIDA", orden.getEstado().getNombre());
        assertTrue(orden.estaActiva());
    }

    @Test
    void recorreElFlujoCompletoHastaEntregada() {
        OrdenServicio orden = ordenNueva();
        orden.iniciarDiagnostico();
        orden.enviarAAprobacion();
        orden.iniciarReparacion();
        orden.finalizar();
        assertNull(orden.getFechaEntrega());
        orden.entregar();
        assertEquals("ENTREGADA", orden.getEstado().getNombre());
        assertFalse(orden.estaActiva());
        assertNotNull(orden.getFechaEntrega());
    }

    @Test
    void presupuestoRechazadoPermiteVolverADiagnosticoOCancelar() {
        OrdenServicio volver = ordenNueva();
        volver.iniciarDiagnostico();
        volver.enviarAAprobacion();
        volver.volverADiagnostico();
        assertEquals("EN_DIAGNOSTICO", volver.getEstado().getNombre());

        OrdenServicio cancelar = ordenNueva();
        cancelar.iniciarDiagnostico();
        cancelar.enviarAAprobacion();
        cancelar.cancelar();
        assertEquals("CANCELADA", cancelar.getEstado().getNombre());
        assertFalse(cancelar.estaActiva());
    }

    @Test
    void noSePuedeSaltarDeRecibidaAReparacion() {
        OrdenServicio orden = ordenNueva();
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class, orden::iniciarReparacion);
        assertEquals("No se puede iniciar la reparación cuando la orden está en estado RECIBIDA.",
                error.getMessage());
        assertEquals("RECIBIDA", orden.getEstado().getNombre());
    }

    @Test
    void noSePuedeCancelarUnaOrdenEnReparacion() {
        OrdenServicio orden = ordenNueva();
        orden.iniciarDiagnostico();
        orden.enviarAAprobacion();
        orden.iniciarReparacion();
        assertThrows(ReglaNegocioException.class, orden::cancelar);
        assertEquals("EN_REPARACION", orden.getEstado().getNombre());
    }

    @Test
    void entregadaYCanceladaSonFinales() {
        assertEquals(0, contarPermitidas(new Entregada()));
        assertEquals(0, contarPermitidas(new Cancelada()));
    }

    @Test
    void cadaEstadoPermiteSoloLasTransicionesAprobadas() {
        assertEquals(2, contarPermitidas(new Recibida()));
        assertEquals(2, contarPermitidas(new EnDiagnostico()));
        assertEquals(3, contarPermitidas(new EsperandoAprobacion()));
        assertEquals(1, contarPermitidas(new EnReparacion()));
        assertEquals(1, contarPermitidas(new Finalizada()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"RECIBIDA", "EN_DIAGNOSTICO", "ESPERANDO_APROBACION", "EN_REPARACION",
            "FINALIZADA", "ENTREGADA", "CANCELADA"})
    void reconstruyeCadaEstadoDesdeSuNombre(String nombre) {
        assertEquals(nombre, EstadoOrden.desde(nombre).getNombre());
    }

    @Test
    void rechazaUnNombreDeEstadoDesconocido() {
        assertThrows(ReglaNegocioException.class, () -> EstadoOrden.desde("INVENTADO"));
    }
}
