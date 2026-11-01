package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.jdbc.ConsumoRepuestoDaoJdbc;
import com.taller.nucleo.dao.jdbc.OrdenServicioDaoJdbc;
import com.taller.nucleo.dao.jdbc.ProveedorDaoJdbc;
import com.taller.nucleo.dao.jdbc.RepuestoDaoJdbc;
import com.taller.nucleo.entidad.Compra;
import com.taller.nucleo.entidad.ConsumoRepuesto;
import com.taller.nucleo.entidad.HistorialEstadoOrden;
import com.taller.nucleo.entidad.MetodoPago;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Prioridad;
import com.taller.nucleo.entidad.Presupuesto;
import com.taller.nucleo.entidad.Proveedor;
import com.taller.nucleo.entidad.Repuesto;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.TipoAtencion;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.entidad.Vehiculo;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.soporte.BaseBD;
import com.taller.nucleo.soporte.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Las 5 reglas de negocio: para cada una, un caso válido y uno que debe ser bloqueado. */
class ReglasNegocioTest extends BaseBD {

    private final OrdenServicioServicio ordenes = new OrdenServicioServicio();
    private final PresupuestoServicio presupuestos = new PresupuestoServicio();
    private final InventarioServicio inventario = new InventarioServicio();
    private final PagoServicio pagos = new PagoServicio();

    private Datos datos;
    private Usuario recepcion;
    private Usuario admin;
    private Usuario encargado;

    @BeforeEach
    void preparar() {
        datos = new Datos(conexion());
        recepcion = datos.usuario(Rol.RECEPCIONISTA);
        admin = datos.usuario(Rol.ADMINISTRADOR);
        encargado = datos.usuario(Rol.ENCARGADO_INVENTARIO);
    }

    // ---------- ayudas ----------

    private OrdenServicio ordenNueva() {
        return ordenes.crear(datos.vehiculo(), datos.tipoAtencion(), Prioridad.MEDIA, recepcion, null);
    }

    /** Orden en ESPERANDO_APROBACION con un presupuesto PENDIENTE de 100.00. */
    private Presupuesto ordenEsperandoAprobacion(OrdenServicio orden) {
        ordenes.iniciarDiagnostico(orden.getId(), recepcion);
        Presupuesto presupuesto = new Presupuesto(orden);
        presupuesto.agregarServicio(datos.servicio("50.00"), new BigDecimal("2"));
        presupuestos.crear(presupuesto, recepcion);
        ordenes.enviarAAprobacion(orden.getId(), recepcion);
        return presupuesto;
    }

    private OrdenServicio ordenEnReparacion() {
        OrdenServicio orden = ordenNueva();
        Presupuesto presupuesto = ordenEsperandoAprobacion(orden);
        presupuestos.aprobar(presupuesto.getId(), recepcion);
        ordenes.iniciarReparacion(orden.getId(), recepcion);
        return orden;
    }

    private OrdenServicio ordenFinalizada() {
        OrdenServicio orden = ordenEnReparacion();
        ordenes.finalizar(orden.getId(), recepcion);
        return orden;
    }

    private String estadoGuardado(OrdenServicio orden) {
        return new OrdenServicioDaoJdbc(conexion()).buscarPorId(orden.getId()).orElseThrow()
                .getEstado().getNombre();
    }

    private int stockDe(Repuesto repuesto) {
        return new RepuestoDaoJdbc(conexion()).buscarPorId(repuesto.getId()).orElseThrow().getStock();
    }

    @Test
    void elTecnicoPuedeFinalizarUnaOrdenPeroElEncargadoDeInventarioNo() {
        OrdenServicio orden = ordenEnReparacion();
        Usuario tecnico = datos.usuario(Rol.TECNICO);

        assertThrows(com.taller.nucleo.excepcion.AccesoDenegadoException.class,
                () -> ordenes.finalizar(orden.getId(), encargado));
        ordenes.finalizar(orden.getId(), tecnico);
        assertEquals("FINALIZADA", estadoGuardado(orden));
        assertThrows(com.taller.nucleo.excepcion.AccesoDenegadoException.class,
                () -> ordenes.entregar(orden.getId(), tecnico));
    }

    // ---------- Regla 1: no dos órdenes activas del mismo tipo sin autorización ----------

    @Nested
    class Regla1OrdenDuplicada {

        @Test
        void sinOrdenActivaPreviaLaOrdenSeCrea() {
            OrdenServicio orden = ordenNueva();
            assertNotNull(orden.getId());
            assertEquals("RECIBIDA", estadoGuardado(orden));
        }

        @Test
        void segundaOrdenActivaDelMismoTipoSinAutorizacionSeBloquea() {
            Vehiculo vehiculo = datos.vehiculo();
            TipoAtencion tipo = datos.tipoAtencion();
            ordenes.crear(vehiculo, tipo, Prioridad.MEDIA, recepcion, null);

            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> ordenes.crear(vehiculo, tipo, Prioridad.ALTA, recepcion, null));
            assertTrue(error.getMessage().contains("autorización de un administrador"));
            assertEquals(1, new OrdenServicioDaoJdbc(conexion()).listarPorVehiculo(vehiculo.getId()).size());
        }

        @Test
        void unRecepcionistaNoPuedeAutorizarLaDuplicada() {
            Vehiculo vehiculo = datos.vehiculo();
            TipoAtencion tipo = datos.tipoAtencion();
            ordenes.crear(vehiculo, tipo, Prioridad.MEDIA, recepcion, null);

            assertThrows(ReglaNegocioException.class,
                    () -> ordenes.crear(vehiculo, tipo, Prioridad.MEDIA, recepcion, recepcion));
        }

        @Test
        void unAdministradorPuedeAutorizarLaDuplicadaYQuedaRegistrado() {
            Vehiculo vehiculo = datos.vehiculo();
            TipoAtencion tipo = datos.tipoAtencion();
            ordenes.crear(vehiculo, tipo, Prioridad.MEDIA, recepcion, null);

            OrdenServicio segunda = ordenes.crear(vehiculo, tipo, Prioridad.MEDIA, recepcion, admin);

            OrdenServicio guardada = new OrdenServicioDaoJdbc(conexion()).buscarPorId(segunda.getId()).orElseThrow();
            assertEquals(admin.getId(), guardada.getAutorizaDuplicada().getId());
        }

        @Test
        void otroTipoDeAtencionNoNecesitaAutorizacion() {
            Vehiculo vehiculo = datos.vehiculo();
            ordenes.crear(vehiculo, datos.tipoAtencion(), Prioridad.MEDIA, recepcion, null);
            assertNotNull(ordenes.crear(vehiculo, datos.tipoAtencion(), Prioridad.MEDIA, recepcion, null).getId());
        }

        @Test
        void trasCancelarLaPrimeraYaSePuedeCrearOtra() {
            Vehiculo vehiculo = datos.vehiculo();
            TipoAtencion tipo = datos.tipoAtencion();
            OrdenServicio primera = ordenes.crear(vehiculo, tipo, Prioridad.MEDIA, recepcion, null);
            ordenes.cancelar(primera.getId(), recepcion);
            assertNotNull(ordenes.crear(vehiculo, tipo, Prioridad.MEDIA, recepcion, null).getId());
        }
    }

    // ---------- Regla 2: no reparar sin presupuesto aprobado ----------

    @Nested
    class Regla2PresupuestoAprobado {

        @Test
        void conPresupuestoAprobadoLaReparacionSeInicia() {
            OrdenServicio orden = ordenNueva();
            Presupuesto presupuesto = ordenEsperandoAprobacion(orden);
            presupuestos.aprobar(presupuesto.getId(), recepcion);

            ordenes.iniciarReparacion(orden.getId(), recepcion);
            assertEquals("EN_REPARACION", estadoGuardado(orden));
        }

        @Test
        void conPresupuestoPendienteLaReparacionSeBloqueaYNadaCambia() {
            OrdenServicio orden = ordenNueva();
            ordenEsperandoAprobacion(orden);
            int cambiosAntes = ordenes.historial(orden.getId()).size();

            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> ordenes.iniciarReparacion(orden.getId(), recepcion));
            assertTrue(error.getMessage().contains("presupuesto aprobado"));
            assertEquals("ESPERANDO_APROBACION", estadoGuardado(orden));
            assertEquals(cambiosAntes, ordenes.historial(orden.getId()).size());
        }

        @Test
        void conPresupuestoRechazadoLaReparacionSeBloqueaYLaOrdenSigueEsperando() {
            OrdenServicio orden = ordenNueva();
            Presupuesto presupuesto = ordenEsperandoAprobacion(orden);
            presupuestos.rechazar(presupuesto.getId(), recepcion);

            assertThrows(ReglaNegocioException.class, () -> ordenes.iniciarReparacion(orden.getId(), recepcion));
            assertEquals("ESPERANDO_APROBACION", estadoGuardado(orden));
        }

        @Test
        void trasUnRechazoSePuedeVolverADiagnosticoYArmarOtroPresupuesto() {
            OrdenServicio orden = ordenNueva();
            Presupuesto primero = ordenEsperandoAprobacion(orden);
            presupuestos.rechazar(primero.getId(), recepcion);

            ordenes.volverADiagnostico(orden.getId(), recepcion);
            Presupuesto segundo = new Presupuesto(orden);
            segundo.agregarServicio(datos.servicio("30.00"), BigDecimal.ONE);
            presupuestos.crear(segundo, recepcion);
            assertEquals(2, new com.taller.nucleo.dao.jdbc.PresupuestoDaoJdbc(conexion())
                    .listarPorOrden(orden.getId()).size(), "El presupuesto rechazado se conserva");
        }

        @Test
        void noSeEnviaAAprobacionSinPresupuestoPendiente() {
            OrdenServicio orden = ordenNueva();
            ordenes.iniciarDiagnostico(orden.getId(), recepcion);
            assertThrows(ReglaNegocioException.class, () -> ordenes.enviarAAprobacion(orden.getId(), recepcion));
            assertEquals("EN_DIAGNOSTICO", estadoGuardado(orden));
        }

        @Test
        void unPresupuestoVacioSeRechaza() {
            OrdenServicio orden = ordenNueva();
            ordenes.iniciarDiagnostico(orden.getId(), recepcion);
            assertThrows(ReglaNegocioException.class, () -> presupuestos.crear(new Presupuesto(orden), recepcion));
        }
    }

    // ---------- Regla 3: el stock baja solo con el consumo real ----------

    @Nested
    class Regla3ConsumoDeRepuestos {

        @Test
        void presupuestarUnRepuestoNoDescuentaStock() {
            OrdenServicio orden = ordenNueva();
            ordenes.iniciarDiagnostico(orden.getId(), recepcion);
            Repuesto repuesto = datos.repuesto(10, "8.00");
            Presupuesto presupuesto = new Presupuesto(orden);
            presupuesto.agregarRepuesto(repuesto, 4);
            presupuestos.crear(presupuesto, recepcion);

            assertEquals(10, stockDe(repuesto));
        }

        @Test
        void elConsumoRealDescuentaElStockYGuardaElPrecio() {
            OrdenServicio orden = ordenEnReparacion();
            Repuesto repuesto = datos.repuesto(10, "8.00");

            inventario.registrarConsumo(orden.getId(), repuesto.getId(), 4, encargado);

            assertEquals(6, stockDe(repuesto));
            List<ConsumoRepuesto> consumos = new ConsumoRepuestoDaoJdbc(conexion()).listarPorOrden(orden.getId());
            assertEquals(1, consumos.size());
            assertEquals(new BigDecimal("8.00"), consumos.get(0).getPrecioUnitario());
        }

        @Test
        void consumirMasDeLoQueHayEsBloqueadoYNadaCambia() {
            OrdenServicio orden = ordenEnReparacion();
            Repuesto repuesto = datos.repuesto(3, "8.00");

            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> inventario.registrarConsumo(orden.getId(), repuesto.getId(), 5, encargado));
            assertTrue(error.getMessage().contains("Stock insuficiente"));
            assertEquals(3, stockDe(repuesto));
            assertTrue(new ConsumoRepuestoDaoJdbc(conexion()).listarPorOrden(orden.getId()).isEmpty());
        }

        @Test
        void soloSeConsumeEnUnaOrdenEnReparacion() {
            OrdenServicio orden = ordenNueva();
            Repuesto repuesto = datos.repuesto(10, "8.00");
            assertThrows(ReglaNegocioException.class,
                    () -> inventario.registrarConsumo(orden.getId(), repuesto.getId(), 1, encargado));
            assertEquals(10, stockDe(repuesto));
        }

        @Test
        void unaCantidadInvalidaSeRechaza() {
            OrdenServicio orden = ordenEnReparacion();
            Repuesto repuesto = datos.repuesto(10, "8.00");
            assertThrows(ReglaNegocioException.class,
                    () -> inventario.registrarConsumo(orden.getId(), repuesto.getId(), 0, encargado));
        }

        @Test
        void laCompraSumaStockYUnaCompraVaciaSeRechaza() {
            Proveedor proveedor = new ProveedorDaoJdbc(conexion()).insertar(new Proveedor(unico("Prov ")));
            Repuesto repuesto = datos.repuesto(2, "8.00");
            Compra compra = new Compra(proveedor, admin);
            compra.agregarDetalle(repuesto, 5, new BigDecimal("3.00"));

            inventario.registrarCompra(compra);
            assertEquals(7, stockDe(repuesto));

            assertThrows(ReglaNegocioException.class,
                    () -> inventario.registrarCompra(new Compra(proveedor, admin)));
        }
    }

    // ---------- Regla 4: entrega solo finalizada y con saldo cero o crédito ----------

    @Nested
    class Regla4Entrega {

        @Test
        void sinFinalizarLaEntregaSeBloquea() {
            OrdenServicio orden = ordenEnReparacion();
            assertThrows(ReglaNegocioException.class, () -> ordenes.entregar(orden.getId(), recepcion));
            assertEquals("EN_REPARACION", estadoGuardado(orden));
        }

        @Test
        void finalizadaPeroConSaldoPendienteSeBloquea() {
            OrdenServicio orden = ordenFinalizada();
            pagos.registrar(orden.getId(), new BigDecimal("40.00"), MetodoPago.EFECTIVO, recepcion);

            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> ordenes.entregar(orden.getId(), recepcion));
            assertTrue(error.getMessage().contains("saldo pendiente de 60.00"));
            assertEquals("FINALIZADA", estadoGuardado(orden));
        }

        @Test
        void finalizadaConSaldoCeroSeEntrega() {
            OrdenServicio orden = ordenFinalizada();
            pagos.registrar(orden.getId(), new BigDecimal("100.00"), MetodoPago.TARJETA, recepcion);
            assertEquals(0, BigDecimal.ZERO.compareTo(pagos.saldo(orden.getId())));

            OrdenServicio entregada = ordenes.entregar(orden.getId(), recepcion);
            assertEquals("ENTREGADA", estadoGuardado(orden));
            assertNotNull(entregada.getFechaEntrega());
        }

        @Test
        void conCreditoAutorizadoPorAdministradorSeEntregaConSaldo() {
            OrdenServicio orden = ordenFinalizada();
            ordenes.autorizarCredito(orden.getId(), admin);

            ordenes.entregar(orden.getId(), recepcion);
            assertEquals("ENTREGADA", estadoGuardado(orden));
        }

        @Test
        void elCreditoAutorizadoSobreviveAlRecargarLaOrdenYPermiteEntregar() {
            OrdenServicio orden = ordenFinalizada();
            assertTrue(!new OrdenServicioDaoJdbc(conexion()).buscarPorId(orden.getId()).orElseThrow()
                    .tieneCreditoAutorizado());

            ordenes.autorizarCredito(orden.getId(), admin);

            OrdenServicio recargada = new OrdenServicioDaoJdbc(conexion()).buscarPorId(orden.getId()).orElseThrow();
            assertTrue(recargada.tieneCreditoAutorizado());
            assertEquals(admin.getId(), recargada.getAutorizaCredito().getId());
            assertEquals(new BigDecimal("100.00"), pagos.saldo(orden.getId()), "El saldo sigue pendiente");

            ordenes.entregar(orden.getId(), recepcion);
            assertEquals("ENTREGADA", estadoGuardado(orden));
        }

        @Test
        void unRecepcionistaNoPuedeAutorizarElCredito() {
            OrdenServicio orden = ordenFinalizada();
            assertThrows(ReglaNegocioException.class, () -> ordenes.autorizarCredito(orden.getId(), recepcion));
            assertThrows(ReglaNegocioException.class, () -> ordenes.entregar(orden.getId(), recepcion));
        }

        @Test
        void noSeCobraMasQueElSaldoNiSinPresupuestoAprobado() {
            OrdenServicio sinPresupuesto = ordenNueva();
            assertThrows(ReglaNegocioException.class, () -> pagos.registrar(
                    sinPresupuesto.getId(), BigDecimal.TEN, MetodoPago.EFECTIVO, recepcion));

            OrdenServicio orden = ordenEnReparacion();
            assertThrows(ReglaNegocioException.class, () -> pagos.registrar(
                    orden.getId(), new BigDecimal("100.01"), MetodoPago.EFECTIVO, recepcion));
            assertEquals(new BigDecimal("100.00"), pagos.saldo(orden.getId()));
        }
    }

    // ---------- Regla 5: historial de cambios de estado ----------

    @Nested
    class Regla5Historial {

        @Test
        void cadaCambioQuedaConEstadosFechaYResponsable() {
            OrdenServicio orden = ordenNueva();
            ordenes.iniciarDiagnostico(orden.getId(), admin);
            ordenes.cancelar(orden.getId(), recepcion);

            List<HistorialEstadoOrden> historial = ordenes.historial(orden.getId());
            assertEquals(3, historial.size());
            assertNull(historial.get(0).getEstadoAnterior());
            assertEquals("RECIBIDA", historial.get(0).getEstadoNuevo());
            assertEquals("RECIBIDA", historial.get(1).getEstadoAnterior());
            assertEquals("EN_DIAGNOSTICO", historial.get(1).getEstadoNuevo());
            assertEquals(admin.getId(), historial.get(1).getResponsable().getId());
            assertEquals("CANCELADA", historial.get(2).getEstadoNuevo());
            assertEquals(recepcion.getId(), historial.get(2).getResponsable().getId());
            assertNotNull(historial.get(2).getFecha());
        }

        @Test
        void unCambioNoPermitidoNoDejaHistorial() {
            OrdenServicio orden = ordenNueva();
            assertThrows(ReglaNegocioException.class, () -> ordenes.finalizar(orden.getId(), recepcion));
            assertEquals(1, ordenes.historial(orden.getId()).size());
        }

        @Test
        void sinResponsableNoSeCambiaElEstado() {
            OrdenServicio orden = ordenNueva();
            assertThrows(ReglaNegocioException.class, () -> ordenes.iniciarDiagnostico(orden.getId(), null));
            assertEquals("RECIBIDA", estadoGuardado(orden));
        }

        @Test
        void elFlujoCompletoRegistraTodosLosPasos() {
            OrdenServicio orden = ordenFinalizada();
            pagos.registrar(orden.getId(), new BigDecimal("100.00"), MetodoPago.EFECTIVO, recepcion);
            ordenes.entregar(orden.getId(), recepcion);

            List<String> nuevos = ordenes.historial(orden.getId()).stream()
                    .map(HistorialEstadoOrden::getEstadoNuevo).toList();
            assertEquals(List.of("RECIBIDA", "EN_DIAGNOSTICO", "ESPERANDO_APROBACION", "EN_REPARACION",
                    "FINALIZADA", "ENTREGADA"), nuevos);
        }
    }
}
