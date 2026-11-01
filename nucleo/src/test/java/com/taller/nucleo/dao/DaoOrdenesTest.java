package com.taller.nucleo.dao;

import com.taller.nucleo.dao.jdbc.AsignacionDaoJdbc;
import com.taller.nucleo.dao.jdbc.AuditoriaDaoJdbc;
import com.taller.nucleo.dao.jdbc.CompraDaoJdbc;
import com.taller.nucleo.dao.jdbc.ConsumoRepuestoDaoJdbc;
import com.taller.nucleo.dao.jdbc.DiagnosticoDaoJdbc;
import com.taller.nucleo.dao.jdbc.HistorialEstadoOrdenDaoJdbc;
import com.taller.nucleo.dao.jdbc.ObservacionOrdenDaoJdbc;
import com.taller.nucleo.dao.jdbc.OrdenServicioDaoJdbc;
import com.taller.nucleo.dao.jdbc.PagoDaoJdbc;
import com.taller.nucleo.dao.jdbc.PresupuestoDaoJdbc;
import com.taller.nucleo.dao.jdbc.ProveedorDaoJdbc;
import com.taller.nucleo.entidad.Asignacion;
import com.taller.nucleo.entidad.Auditoria;
import com.taller.nucleo.entidad.Compra;
import com.taller.nucleo.entidad.ConsumoRepuesto;
import com.taller.nucleo.entidad.Diagnostico;
import com.taller.nucleo.entidad.EstadoPresupuesto;
import com.taller.nucleo.entidad.EstadoTarea;
import com.taller.nucleo.entidad.HistorialEstadoOrden;
import com.taller.nucleo.entidad.MetodoPago;
import com.taller.nucleo.entidad.ObservacionOrden;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Pago;
import com.taller.nucleo.entidad.Presupuesto;
import com.taller.nucleo.entidad.Proveedor;
import com.taller.nucleo.entidad.Repuesto;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Servicio;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.soporte.BaseBD;
import com.taller.nucleo.soporte.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DaoOrdenesTest extends BaseBD {

    private Datos datos;
    private Usuario recepcion;
    private Usuario admin;
    private OrdenServicioDao ordenes;

    @BeforeEach
    void preparar() {
        datos = new Datos(conexion());
        recepcion = datos.usuario(Rol.RECEPCIONISTA);
        admin = datos.usuario(Rol.ADMINISTRADOR);
        ordenes = new OrdenServicioDaoJdbc(conexion());
    }

    @Test
    void ordenSeGuardaConSuEstadoYSeReconstruyeIgual() {
        OrdenServicio orden = datos.orden(recepcion);
        orden.autorizarCredito(admin);
        orden.iniciarDiagnostico();
        ordenes.actualizar(orden);

        OrdenServicio leida = ordenes.buscarPorId(orden.getId()).orElseThrow();
        assertEquals("EN_DIAGNOSTICO", leida.getEstado().getNombre());
        assertEquals(orden.getVehiculo().getPlaca(), leida.getVehiculo().getPlaca());
        assertEquals(recepcion.getId(), leida.getUsuarioRegistro().getId());
        assertEquals(admin.getId(), leida.getAutorizaCredito().getId());
        assertTrue(leida.tieneCreditoAutorizado());
        assertNull(leida.getAutorizaDuplicada());
        // La orden reconstruida sigue obedeciendo el patrón State.
        leida.enviarAAprobacion();
        assertEquals("ESPERANDO_APROBACION", leida.getEstado().getNombre());
    }

    @Test
    void existeActivaDetectaOrdenesAbiertasDelMismoTipo() {
        OrdenServicio orden = datos.orden(recepcion);
        int vehiculo = orden.getVehiculo().getId();
        int tipo = orden.getTipoAtencion().getId();
        assertTrue(ordenes.existeActiva(vehiculo, tipo));

        orden.cancelar();
        ordenes.actualizar(orden);
        assertFalse(ordenes.existeActiva(vehiculo, tipo));
    }

    @Test
    void buscarFiltraPorEstadoYPorTexto() {
        OrdenServicio orden = datos.orden(recepcion);
        String placa = orden.getVehiculo().getPlaca();
        assertEquals(1, ordenes.buscar("RECIBIDA", placa).size());
        assertTrue(ordenes.buscar("CANCELADA", placa).isEmpty());
        assertEquals(1, ordenes.buscar(null, orden.getVehiculo().getPropietario().getNombre()).size());
        assertEquals(1, ordenes.listarPorVehiculo(orden.getVehiculo().getId()).size());
    }

    @Test
    void historialSeGuardaEnOrdenCronologico() {
        OrdenServicio orden = datos.orden(recepcion);
        HistorialEstadoOrdenDao dao = new HistorialEstadoOrdenDaoJdbc(conexion());
        dao.insertar(new HistorialEstadoOrden(orden, null, "RECIBIDA", recepcion));
        dao.insertar(new HistorialEstadoOrden(orden, "RECIBIDA", "EN_DIAGNOSTICO", admin));

        var historial = dao.listarPorOrden(orden.getId());
        assertEquals(2, historial.size());
        assertNull(historial.get(0).getEstadoAnterior());
        assertEquals("EN_DIAGNOSTICO", historial.get(1).getEstadoNuevo());
        assertEquals(admin.getId(), historial.get(1).getResponsable().getId());
        assertNotNull(historial.get(1).getFecha());
    }

    @Test
    void diagnosticoAsignacionYObservacionSePersisten() {
        OrdenServicio orden = datos.orden(recepcion);
        Tecnico tecnico = datos.tecnico();

        DiagnosticoDao diagnosticos = new DiagnosticoDaoJdbc(conexion());
        Diagnostico diagnostico = diagnosticos.insertar(new Diagnostico(orden, tecnico, "Falla en frenos"));
        diagnostico.setDescripcion("Frenos y suspensión");
        diagnosticos.actualizar(diagnostico);
        assertEquals("Frenos y suspensión", diagnosticos.listarPorOrden(orden.getId()).get(0).getDescripcion());

        AsignacionDao asignaciones = new AsignacionDaoJdbc(conexion());
        Asignacion asignacion = asignaciones.insertar(new Asignacion(orden, tecnico));
        asignacion.actualizarAvance(60);
        asignaciones.actualizar(asignacion);
        Asignacion leida = asignaciones.listarPorTecnico(tecnico.getId()).get(0);
        assertEquals(EstadoTarea.EN_CURSO, leida.getEstadoTarea());
        assertEquals(60, leida.getPorcentajeAvance());

        ObservacionOrdenDao observaciones = new ObservacionOrdenDaoJdbc(conexion());
        observaciones.insertar(new ObservacionOrden(orden, tecnico, "Pastillas desgastadas"));
        assertEquals("Pastillas desgastadas", observaciones.listarPorOrden(orden.getId()).get(0).getTexto());
    }

    @Test
    void presupuestoSeGuardaConSusDetallesYSePuedeAprobar() {
        OrdenServicio orden = datos.orden(recepcion);
        Servicio servicio = datos.servicio("20.00");
        Repuesto repuesto = datos.repuesto(10, "8.00");
        Presupuesto presupuesto = new Presupuesto(orden);
        presupuesto.agregarServicio(servicio, new BigDecimal("2"));
        presupuesto.agregarRepuesto(repuesto, 3);

        PresupuestoDao dao = new PresupuestoDaoJdbc(conexion());
        dao.insertar(presupuesto);
        assertTrue(dao.buscarAprobadoDeOrden(orden.getId()).isEmpty());

        Presupuesto leido = dao.buscarPorId(presupuesto.getId()).orElseThrow();
        assertEquals(new BigDecimal("64.00"), leido.total());
        assertEquals(1, leido.getServicios().size());
        assertEquals(3, leido.getRepuestos().get(0).cantidad());
        assertEquals(10, repuesto.getStock(), "Presupuestar no descuenta stock");

        leido.aprobar(admin);
        dao.actualizarEstado(leido);
        Presupuesto aprobado = dao.buscarAprobadoDeOrden(orden.getId()).orElseThrow();
        assertEquals(EstadoPresupuesto.APROBADO, aprobado.getEstado());
        assertEquals(admin.getId(), aprobado.getAprobador().getId());
        assertEquals(1, dao.listarPorOrden(orden.getId()).size());
    }

    @Test
    void consumoYPagosSePersistenYSeSuman() {
        OrdenServicio orden = datos.orden(recepcion);
        Repuesto repuesto = datos.repuesto(10, "8.00");

        ConsumoRepuestoDao consumos = new ConsumoRepuestoDaoJdbc(conexion());
        consumos.insertar(new ConsumoRepuesto(orden, repuesto, recepcion, 2, new BigDecimal("8.00")));
        assertEquals(2, consumos.listarPorOrden(orden.getId()).get(0).getCantidad());

        PagoDao pagos = new PagoDaoJdbc(conexion());
        assertEquals(0, BigDecimal.ZERO.compareTo(pagos.totalPagado(orden.getId())));
        pagos.insertar(new Pago(orden, recepcion, new BigDecimal("30.00"), MetodoPago.EFECTIVO));
        pagos.insertar(new Pago(orden, recepcion, new BigDecimal("12.50"), MetodoPago.TARJETA));
        assertEquals(0, new BigDecimal("42.50").compareTo(pagos.totalPagado(orden.getId())));
        assertEquals(2, pagos.listarPorOrden(orden.getId()).size());
    }

    @Test
    void compraSeGuardaConSusDetalles() {
        Proveedor proveedor = new ProveedorDaoJdbc(conexion()).insertar(new Proveedor(unico("Prov ")));
        Repuesto repuesto = datos.repuesto(0, "8.00");
        Compra compra = new Compra(proveedor, admin);
        compra.agregarDetalle(repuesto, 4, new BigDecimal("2.50"));

        CompraDao dao = new CompraDaoJdbc(conexion());
        dao.insertar(compra);
        Compra leida = dao.buscarPorId(compra.getId()).orElseThrow();
        assertEquals(new BigDecimal("10.00"), leida.total());
        assertEquals(repuesto.getId(), leida.getDetalles().get(0).repuesto().getId());
    }

    @Test
    void auditoriaSeConsultaPorUsuarioYPorTabla() {
        AuditoriaDao dao = new AuditoriaDaoJdbc(conexion());
        dao.insertar(new Auditoria(admin, "CREAR", "orden_servicio", 7));
        dao.insertar(new Auditoria(admin, "LOGIN", "usuario", null));

        assertEquals(2, dao.listarPorUsuario(admin.getId()).size());
        assertEquals(1, dao.listarPorTabla("orden_servicio").stream()
                .filter(a -> a.getUsuario().getId().equals(admin.getId())).count());
        assertNull(dao.listarPorTabla("usuario").stream()
                .filter(a -> a.getUsuario().getId().equals(admin.getId())).findFirst().orElseThrow().getIdRegistro());
    }
}
