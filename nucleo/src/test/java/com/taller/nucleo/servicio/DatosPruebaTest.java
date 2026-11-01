package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.jdbc.OrdenServicioDaoJdbc;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.reporte.Reporte;
import com.taller.nucleo.soporte.BaseBD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Carga database/datos_prueba.sql dentro de la transacción de la prueba (se revierte al final)
 * y comprueba que los datos respetan las reglas del negocio y alimentan los reportes.
 * Requiere la base de pruebas vacía.
 */
class DatosPruebaTest extends BaseBD {

    @BeforeEach
    void cargarScript() throws IOException, SQLException {
        Path ruta = Path.of("..", "database", "datos_prueba.sql");
        if (!Files.exists(ruta)) {
            ruta = Path.of("database", "datos_prueba.sql");
        }
        StringBuilder sql = new StringBuilder();
        for (String linea : Files.readAllLines(ruta, StandardCharsets.UTF_8)) {
            if (!linea.startsWith("--") && !linea.startsWith("USE ")) {
                sql.append(linea).append('\n');
            }
        }
        try (Statement sentencia = conexion().createStatement()) {
            for (String instruccion : sql.toString().split(";\\s*\\n")) {
                if (!instruccion.isBlank()) {
                    sentencia.execute(instruccion);
                }
            }
        }
    }

    private long numero(String consulta) throws SQLException {
        try (Statement s = conexion().createStatement(); ResultSet r = s.executeQuery(consulta)) {
            r.next();
            return r.getLong(1);
        }
    }

    private List<Integer> ids(String consulta) throws SQLException {
        List<Integer> lista = new ArrayList<>();
        try (Statement s = conexion().createStatement(); ResultSet r = s.executeQuery(consulta)) {
            while (r.next()) {
                lista.add(r.getInt(1));
            }
        }
        return lista;
    }

    @Test
    void cargaLosRegistrosEsperados() throws SQLException {
        assertEquals(9, numero("SELECT COUNT(*) FROM orden_servicio"));
        assertEquals(6, numero("SELECT COUNT(*) FROM presupuesto"));
        assertEquals(6, numero("SELECT COUNT(*) FROM vehiculo"));
        assertEquals(5, numero("SELECT COUNT(*) FROM usuario"));
    }

    @Test
    void elStockEsLoCompradoMenosLoConsumido() throws SQLException {
        assertEquals(0, numero("SELECT COUNT(*) FROM repuesto r WHERE r.stock <> "
                + "COALESCE((SELECT SUM(d.cantidad) FROM detalle_compra d WHERE d.id_repuesto = r.id), 0) - "
                + "COALESCE((SELECT SUM(c.cantidad) FROM consumo_repuesto c WHERE c.id_repuesto = r.id), 0)"));
    }

    @Test
    void ningunVehiculoTieneDosOrdenesActivasDelMismoTipo() throws SQLException {
        assertEquals(0, numero("SELECT COUNT(*) FROM (SELECT id_vehiculo, id_tipo_atencion FROM orden_servicio "
                + "WHERE estado NOT IN ('ENTREGADA', 'CANCELADA') GROUP BY id_vehiculo, id_tipo_atencion "
                + "HAVING COUNT(*) > 1) duplicadas"));
    }

    @Test
    void todaReparacionTienePresupuestoAprobado() throws SQLException {
        assertEquals(0, numero("SELECT COUNT(*) FROM orden_servicio o WHERE o.estado IN "
                + "('EN_REPARACION', 'FINALIZADA', 'ENTREGADA') AND NOT EXISTS "
                + "(SELECT 1 FROM presupuesto p WHERE p.id_orden = o.id AND p.estado = 'APROBADO')"));
    }

    @Test
    void elHistorialTerminaEnElEstadoActualDeCadaOrden() throws SQLException {
        assertEquals(0, numero("SELECT COUNT(*) FROM orden_servicio o WHERE o.estado <> "
                + "(SELECT h.estado_nuevo FROM historial_estado_orden h WHERE h.id_orden = o.id "
                + "ORDER BY h.fecha DESC, h.id DESC LIMIT 1)"));
    }

    @Test
    void lasOrdenesEntregadasTienenSaldoCeroYLasDemasElSaldoEsperado() throws SQLException {
        PagoServicio pagos = new PagoServicio();
        for (int id : ids("SELECT id FROM orden_servicio WHERE estado = 'ENTREGADA'")) {
            assertEquals(0, BigDecimal.ZERO.compareTo(pagos.saldo(id)), "Orden entregada #" + id);
        }
        assertEquals(new BigDecimal("102.50"), pagos.saldo(3));
        assertEquals(new BigDecimal("105.00"), pagos.saldo(4));
    }

    @Test
    void losTotalesDeLosPresupuestosCoincidenConSusDetalles() throws SQLException {
        assertEquals(0, numero("SELECT COUNT(*) FROM presupuesto p WHERE p.total <> "
                + "COALESCE((SELECT SUM(d.cantidad_horas * d.precio_unitario) FROM detalle_servicio d "
                + "WHERE d.id_presupuesto = p.id), 0) + "
                + "COALESCE((SELECT SUM(r.cantidad * r.precio_unitario) FROM detalle_presupuesto_repuesto r "
                + "WHERE r.id_presupuesto = p.id), 0)"));
    }

    @Test
    void losUsuariosDeEjemploPuedenIniciarSesion() {
        AutenticacionServicio autenticacion = new AutenticacionServicio();
        assertEquals(Rol.ADMINISTRADOR, autenticacion.iniciarSesion("admin", "Admin2026").getRol());
        assertEquals(Rol.RECEPCIONISTA, autenticacion.iniciarSesion("recepcion", "Recep2026").getRol());
        Usuario tecnico = autenticacion.iniciarSesion("tecnico1", "Tecnico2026");
        assertEquals("Luis Ramírez", tecnico.getTecnico().getNombre());
        assertEquals(Rol.ENCARGADO_INVENTARIO, autenticacion.iniciarSesion("inventario", "Inventario2026").getRol());
    }

    @Test
    void losCincoReportesMuestranLosDatosCargados() throws SQLException {
        Usuario admin = new com.taller.nucleo.dao.jdbc.UsuarioDaoJdbc(conexion()).buscarPorNombreUsuario("admin").orElseThrow();
        ReporteServicio reportes = new ReporteServicio();

        Reporte porEstado = reportes.ordenesPorEstado(admin, null, null);
        assertEquals(7, porEstado.filas().size());
        assertEquals(2L, porEstado.filas().stream().filter(f -> "ENTREGADA".equals(f.get(0))).findFirst().orElseThrow().get(1));

        Reporte productividad = reportes.productividadPorTecnico(admin, null, null);
        assertEquals(3, productividad.filas().size());
        // Los tres terminaron una tarea; Jorge y Luis tienen 2 tareas y Marta solo 1.
        assertEquals("Jorge Castillo", productividad.filas().get(0).get(0));
        assertEquals(2L, productividad.filas().get(0).get(1));
        assertEquals("Marta Flores", productividad.filas().get(2).get(0));

        Reporte repuestos = reportes.repuestosMasConsumidos(admin, null, null, 3);
        assertEquals("ACE-5W30", repuestos.filas().get(0).get(0), "El aceite es lo más consumido");

        Reporte ingresos = reportes.ingresosPorServicio(admin, null, null);
        assertEquals(0, new BigDecimal("90").compareTo((BigDecimal) ingresos.filas().get(0).get(3)));
        assertFalse(ingresos.estaVacio());

        OrdenServicio orden = new OrdenServicioDaoJdbc(conexion()).buscarPorId(1).orElseThrow();
        Reporte historial = reportes.historialDeVehiculo(admin, orden.getVehiculo().getId());
        assertEquals(2, historial.filas().size());
        assertTrue(historial.filas().stream().anyMatch(f -> "ENTREGADA".equals(f.get(4))));
    }
}
