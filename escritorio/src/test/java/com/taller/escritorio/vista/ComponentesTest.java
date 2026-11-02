package com.taller.escritorio.vista;

import com.taller.escritorio.controlador.Transicion;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.nucleo.estado.EstadoOrden;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentesTest {

    private record Fila(String nombre, int cantidad) {
    }

    private static TablaDatos<Fila> tabla() {
        TablaDatos<Fila> tabla = new TablaDatos<>(List.of(
                new Columna<>("Nombre", Fila::nombre), new Columna<>("Cantidad", Fila::cantidad)));
        tabla.setDatos(List.of(new Fila("Aceite", 4), new Fila("Bujía", 10), new Fila("Batería", 1)));
        return tabla;
    }

    @Test
    void laTablaFiltraSinDistinguirMayusculasYSinPerderLosDatos() {
        TablaDatos<Fila> tabla = tabla();
        assertEquals(3, tabla.cantidadFilas());
        tabla.filtrar("ba");
        assertNull(tabla.seleccionado(), "Sin elegir nada no hay selección");
        tabla.filtrar("");
        assertEquals(3, tabla.cantidadFilas());
    }

    @Test
    void cadaEstadoOfreceSoloLasTransicionesDelPatronState() {
        assertEquals(List.of(Transicion.INICIAR_DIAGNOSTICO, Transicion.CANCELAR), Transicion.desde("RECIBIDA"));
        assertEquals(List.of(Transicion.FINALIZAR), Transicion.desde("EN_REPARACION"));
        assertEquals(List.of(Transicion.ENTREGAR), Transicion.desde("FINALIZADA"));
        assertTrue(Transicion.desde("ENTREGADA").isEmpty());
        assertTrue(Transicion.desde("CANCELADA").isEmpty());
    }

    @Test
    void lasTransicionesOfrecidasSonLasQueElEstadoRealPermite() {
        for (String nombre : List.of("RECIBIDA", "EN_DIAGNOSTICO", "ESPERANDO_APROBACION", "EN_REPARACION", "FINALIZADA")) {
            for (Transicion transicion : Transicion.desde(nombre)) {
                EstadoOrden estado = EstadoOrden.desde(nombre);
                // Si el estado no permitiera la transición, la llamada lanzaría una excepción de regla de negocio.
                switch (transicion) {
                    case INICIAR_DIAGNOSTICO -> estado.iniciarDiagnostico();
                    case ENVIAR_A_APROBACION -> estado.enviarAAprobacion();
                    case INICIAR_REPARACION -> estado.iniciarReparacion();
                    case VOLVER_A_DIAGNOSTICO -> estado.volverADiagnostico();
                    case FINALIZAR -> estado.finalizar();
                    case ENTREGAR -> estado.entregar();
                    case CANCELAR -> estado.cancelar();
                }
            }
        }
    }
}
