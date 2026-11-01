package com.taller.nucleo.entidad;

import com.taller.nucleo.excepcion.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntidadesNegocioTest {

    private static final Usuario RECEPCION = new Usuario("rosa", "hash", Rol.RECEPCIONISTA);
    private static final Usuario ADMIN = new Usuario("admin", "hash", Rol.ADMINISTRADOR);

    private OrdenServicio orden() {
        Vehiculo vehiculo = new Vehiculo("ABC123", new Cliente("1", "Ana"),
                new Modelo(new Marca("Toyota"), "Corolla"), 2018);
        return new OrdenServicio(vehiculo, new TipoAtencion("Mantenimiento"), Prioridad.MEDIA, RECEPCION);
    }

    @Test
    void repuestoDescuentaStockYRechazaSiNoAlcanza() {
        Repuesto filtro = new Repuesto("F-1", "Filtro", new BigDecimal("5"), new BigDecimal("8"));
        filtro.setStock(3);
        filtro.descontar(2);
        assertEquals(1, filtro.getStock());
        assertThrows(ReglaNegocioException.class, () -> filtro.descontar(2));
        assertEquals(1, filtro.getStock());
    }

    @Test
    void montosNegativosSeRechazan() {
        assertThrows(ReglaNegocioException.class, () -> new Servicio("Cambio de aceite", new BigDecimal("-1")));
        assertThrows(ReglaNegocioException.class,
                () -> new Pago(orden(), RECEPCION, BigDecimal.ZERO, MetodoPago.EFECTIVO));
    }

    @Test
    void presupuestoSumaManoDeObraYRepuestosYSoloSeApruebaUnaVez() {
        Presupuesto presupuesto = new Presupuesto(orden());
        presupuesto.agregarServicio(new Servicio("Alineación", new BigDecimal("20.00")), new BigDecimal("2"));
        presupuesto.agregarRepuesto(new Repuesto("F-1", "Filtro", new BigDecimal("5"), new BigDecimal("8")), 3);
        assertEquals(new BigDecimal("64.00"), presupuesto.total());

        presupuesto.aprobar(ADMIN);
        assertEquals(EstadoPresupuesto.APROBADO, presupuesto.getEstado());
        assertThrows(ReglaNegocioException.class, () -> presupuesto.rechazar(ADMIN));
        assertThrows(ReglaNegocioException.class,
                () -> presupuesto.agregarServicio(new Servicio("Otro", BigDecimal.ONE), BigDecimal.ONE));
    }

    @Test
    void soloUnAdministradorAutorizaExcepciones() {
        OrdenServicio orden = orden();
        assertThrows(ReglaNegocioException.class, () -> orden.autorizarCredito(RECEPCION));
        assertFalse(orden.tieneCreditoAutorizado());
        orden.autorizarCredito(ADMIN);
        assertTrue(orden.tieneCreditoAutorizado());
    }

    @Test
    void asignacionExigeTecnicoActivoYAvanceCoherente() {
        Tecnico tecnico = new Tecnico("Luis");
        Asignacion asignacion = new Asignacion(orden(), tecnico);
        asignacion.actualizarAvance(40);
        assertEquals(EstadoTarea.EN_CURSO, asignacion.getEstadoTarea());
        asignacion.actualizarAvance(100);
        assertEquals(EstadoTarea.TERMINADA, asignacion.getEstadoTarea());
        assertThrows(ReglaNegocioException.class, () -> asignacion.actualizarAvance(101));
        assertThrows(ReglaNegocioException.class, () -> new Asignacion(orden(), null));
    }

    @Test
    void compraSumaSusDetalles() {
        Compra compra = new Compra(new Proveedor("Repuestos SA"), ADMIN);
        compra.agregarDetalle(new Repuesto("F-1", "Filtro", BigDecimal.ONE, BigDecimal.TEN), 4, new BigDecimal("2.50"));
        assertEquals(new BigDecimal("10.00"), compra.total());
        assertThrows(ReglaNegocioException.class,
                () -> compra.agregarDetalle(new Repuesto("F-2", "Otro", BigDecimal.ONE, BigDecimal.TEN), 0, BigDecimal.ONE));
    }
}
