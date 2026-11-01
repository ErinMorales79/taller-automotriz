package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.jdbc.AsignacionDaoJdbc;
import com.taller.nucleo.dao.jdbc.ConsumoRepuestoDaoJdbc;
import com.taller.nucleo.dao.jdbc.DiagnosticoDaoJdbc;
import com.taller.nucleo.dao.jdbc.OrdenServicioDaoJdbc;
import com.taller.nucleo.dao.jdbc.PresupuestoDaoJdbc;
import com.taller.nucleo.entidad.Asignacion;
import com.taller.nucleo.entidad.ConsumoRepuesto;
import com.taller.nucleo.entidad.Diagnostico;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Prioridad;
import com.taller.nucleo.entidad.Presupuesto;
import com.taller.nucleo.entidad.Repuesto;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Servicio;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.entidad.Vehiculo;
import com.taller.nucleo.excepcion.AccesoDenegadoException;
import com.taller.nucleo.excepcion.ExportacionException;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.reporte.Reporte;
import com.taller.nucleo.soporte.BaseBD;
import com.taller.nucleo.soporte.Datos;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Los 5 reportes: valores exactos con datos conocidos, filtros, permisos y exportación. */
class ReporteServicioTest extends BaseBD {

    private final ReporteServicio reportes = new ReporteServicio();

    private Datos datos;
    private Usuario admin;
    private Usuario recepcion;

    @BeforeEach
    void preparar() {
        datos = new Datos(conexion());
        admin = datos.usuario(Rol.ADMINISTRADOR);
        recepcion = datos.usuario(Rol.RECEPCIONISTA);
    }

    // ---------- ayudas ----------

    private static BigDecimal dinero(String valor) {
        return new BigDecimal(valor);
    }

    /** Orden con presupuesto aprobado de un servicio; queda en el estado final pedido. */
    private OrdenServicio ordenConPresupuesto(Vehiculo vehiculo, Servicio servicio, String horas, boolean finalizada) {
        OrdenServicio orden = new OrdenServicioDaoJdbc(conexion()).insertar(
                new OrdenServicio(vehiculo, datos.tipoAtencion(), Prioridad.MEDIA, recepcion));
        orden.iniciarDiagnostico();
        orden.enviarAAprobacion();
        Presupuesto presupuesto = new Presupuesto(orden);
        presupuesto.agregarServicio(servicio, new BigDecimal(horas));
        presupuesto.aprobar(admin);
        new PresupuestoDaoJdbc(conexion()).insertar(presupuesto);
        orden.iniciarReparacion();
        if (finalizada) {
            orden.finalizar();
        }
        new OrdenServicioDaoJdbc(conexion()).actualizar(orden);
        return orden;
    }

    private static List<Object> fila(Reporte reporte, int columna, Object valor) {
        return reporte.filas().stream().filter(f -> valor.equals(f.get(columna))).findFirst().orElseThrow();
    }

    // ---------- reportes ----------

    @Test
    void ingresosPorServicioSoloCuentaOrdenesFinalizadasOEntregadas() {
        Servicio servicio = datos.servicio("50.00");
        ordenConPresupuesto(datos.vehiculo(), servicio, "2", true);
        ordenConPresupuesto(datos.vehiculo(), servicio, "10", false);

        Reporte reporte = reportes.ingresosPorServicio(admin, null, null);

        List<Object> fila = fila(reporte, 0, servicio.getNombre());
        assertEquals(1L, fila.get(1));
        assertEquals(0, dinero("2").compareTo((BigDecimal) fila.get(2)));
        assertEquals(0, dinero("100").compareTo((BigDecimal) fila.get(3)),
                "La orden aún en reparación no se factura");
        assertEquals(List.of("Servicio", "Veces facturado", "Horas", "Ingresos"), reporte.columnas());
    }

    @Test
    void repuestosMasConsumidosSumaUnidadesYMontoYRespetaElLimite() {
        OrdenServicio orden = datos.orden(recepcion);
        Repuesto repuesto = datos.repuesto(100, "8.00");
        ConsumoRepuestoDaoJdbc consumos = new ConsumoRepuestoDaoJdbc(conexion());
        consumos.insertar(new ConsumoRepuesto(orden, repuesto, recepcion, 3, dinero("8.00")));
        consumos.insertar(new ConsumoRepuesto(orden, repuesto, recepcion, 2, dinero("8.00")));

        Reporte reporte = reportes.repuestosMasConsumidos(admin, null, null, 100);
        List<Object> fila = fila(reporte, 0, repuesto.getCodigo());
        assertEquals(0, dinero("5").compareTo(new BigDecimal(fila.get(2).toString())));
        assertEquals(0, dinero("40").compareTo((BigDecimal) fila.get(3)));

        assertEquals(1, reportes.repuestosMasConsumidos(admin, null, null, 1).filas().size());
        assertThrows(ReglaNegocioException.class, () -> reportes.repuestosMasConsumidos(admin, null, null, 0));
        assertThrows(ReglaNegocioException.class, () -> reportes.repuestosMasConsumidos(admin, null, null, 101));
    }

    @Test
    void productividadPorTecnicoCuentaTareasTerminadasYPromedio() {
        Tecnico activo = datos.tecnico();
        Tecnico sinTareas = datos.tecnico();
        AsignacionDaoJdbc asignaciones = new AsignacionDaoJdbc(conexion());
        Asignacion terminada = new Asignacion(datos.orden(recepcion), activo);
        terminada.actualizarAvance(100);
        asignaciones.insertar(terminada);
        Asignacion enCurso = new Asignacion(datos.orden(recepcion), activo);
        enCurso.actualizarAvance(50);
        asignaciones.insertar(enCurso);

        Reporte reporte = reportes.productividadPorTecnico(recepcion, null, null);

        List<Object> fila = fila(reporte, 0, activo.getNombre());
        assertEquals(2L, fila.get(1));
        assertEquals(0, dinero("1").compareTo(new BigDecimal(fila.get(2).toString())));
        assertEquals(2L, fila.get(3));
        assertEquals(0, dinero("75").compareTo(new BigDecimal(fila.get(4).toString())));
        assertEquals(0L, fila(reporte, 0, sinTareas.getNombre()).get(1), "Un técnico sin tareas aparece con ceros");
    }

    @Test
    void ordenesPorEstadoAgrupaYElRangoDeFechasFiltra() {
        datos.orden(recepcion);
        datos.orden(recepcion);

        Reporte reporte = reportes.ordenesPorEstado(admin, null, null);
        assertTrue((Long) fila(reporte, 0, "RECIBIDA").get(1) >= 2);

        LocalDate hoy = LocalDate.now();
        assertTrue((Long) fila(reportes.ordenesPorEstado(admin, hoy, hoy), 0, "RECIBIDA").get(1) >= 2);
        assertTrue(reportes.ordenesPorEstado(admin, hoy.plusDays(1), null).filas().isEmpty());
        assertTrue(reportes.ordenesPorEstado(admin, null, hoy.minusDays(1)).filas().isEmpty());
    }

    @Test
    void historialDelVehiculoListaSusOrdenesDeLaMasRecienteALaMasAntigua() {
        Vehiculo vehiculo = datos.vehiculo();
        Servicio servicio = datos.servicio("50.00");
        OrdenServicio primera = ordenConPresupuesto(vehiculo, servicio, "2", true);
        new DiagnosticoDaoJdbc(conexion()).insertar(new Diagnostico(primera, datos.tecnico(), "Cambio de frenos"));
        OrdenServicio segunda = datos.orden(recepcion);
        segunda = new OrdenServicioDaoJdbc(conexion()).insertar(
                new OrdenServicio(vehiculo, datos.tipoAtencion(), Prioridad.ALTA, recepcion));

        Reporte reporte = reportes.historialDeVehiculo(recepcion, vehiculo.getId());

        assertEquals(2, reporte.filas().size());
        assertEquals(segunda.getId(), reporte.filas().get(0).get(0), "La más reciente va primero");
        assertEquals(0, BigDecimal.ZERO.compareTo((BigDecimal) reporte.filas().get(0).get(6)));
        assertNull(reporte.filas().get(0).get(5), "Sin fecha de entrega queda vacío");
        assertEquals(0, dinero("100").compareTo((BigDecimal) reporte.filas().get(1).get(6)));
        assertEquals("Cambio de frenos", reporte.filas().get(1).get(7));
    }

    // ---------- reglas de acceso y validaciones ----------

    @Test
    void soloLosRolesConPermisoVenLosReportes() {
        Usuario tecnico = datos.usuario(Rol.TECNICO);
        Usuario encargado = datos.usuario(Rol.ENCARGADO_INVENTARIO);

        assertThrows(AccesoDenegadoException.class, () -> reportes.ordenesPorEstado(tecnico, null, null));
        assertThrows(AccesoDenegadoException.class,
                () -> reportes.historialDeVehiculo(tecnico, datos.vehiculo().getId()));
        assertEquals(List.of("Estado", "Órdenes", "Antigüedad promedio (días)", "Antigüedad máxima (días)"),
                reportes.ordenesPorEstado(encargado, null, null).columnas());
    }

    @Test
    void unRangoInvertidoOUnVehiculoInexistenteSeRechazan() {
        LocalDate hoy = LocalDate.now();
        assertThrows(ReglaNegocioException.class, () -> reportes.ingresosPorServicio(admin, hoy, hoy.minusDays(1)));
        assertThrows(ReglaNegocioException.class, () -> reportes.historialDeVehiculo(admin, 2_000_000_000));
    }

    @Test
    void unReporteValidaQueSusFilasCoincidanConSusColumnas() {
        assertThrows(ReglaNegocioException.class, () -> new Reporte("X", List.of("A", "B"), List.of(List.of(1))));
        assertThrows(ReglaNegocioException.class, () -> new Reporte("X", List.of(), List.of()));
        Reporte conNulos = new Reporte("X", List.of("A", "B"), List.of(Arrays.asList(1, null)));
        assertNull(conNulos.filas().get(0).get(1));
    }

    // ---------- exportación ----------

    @Test
    void exportaAPdfYAExcelConElContenidoDelReporte(@TempDir Path carpeta) throws IOException {
        Servicio servicio = datos.servicio("50.00");
        ordenConPresupuesto(datos.vehiculo(), servicio, "2", true);
        Reporte reporte = reportes.ingresosPorServicio(admin, null, null);

        Path pdf = carpeta.resolve("ingresos.pdf");
        reportes.exportarPdf(admin, reporte, pdf);
        assertTrue(Files.size(pdf) > 500);
        assertEquals("%PDF", new String(Files.readAllBytes(pdf), 0, 4, java.nio.charset.StandardCharsets.US_ASCII));

        Path excel = carpeta.resolve("ingresos.xlsx");
        reportes.exportarExcel(admin, reporte, excel);
        try (InputStream entrada = Files.newInputStream(excel); XSSFWorkbook libro = new XSSFWorkbook(entrada)) {
            Sheet hoja = libro.getSheetAt(0);
            assertEquals("Servicio", hoja.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Ingresos", hoja.getRow(0).getCell(3).getStringCellValue());
            boolean encontrado = false;
            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                if (servicio.getNombre().equals(hoja.getRow(i).getCell(0).getStringCellValue())) {
                    assertEquals(100.0, hoja.getRow(i).getCell(3).getNumericCellValue(), 0.001);
                    encontrado = true;
                }
            }
            assertTrue(encontrado, "La fila del servicio debe estar en el Excel");
        }
    }

    @Test
    void exportarUnReporteVacioFuncionaYUnDestinoInvalidoDaUnErrorClaro(@TempDir Path carpeta) {
        Reporte vacio = new Reporte("Reporte vacío", List.of("A"), List.of());
        reportes.exportarPdf(admin, vacio, carpeta.resolve("vacio.pdf"));
        reportes.exportarExcel(admin, vacio, carpeta.resolve("vacio.xlsx"));
        assertTrue(Files.exists(carpeta.resolve("vacio.pdf")));

        Path inexistente = carpeta.resolve("no-existe").resolve("x.pdf");
        ExportacionException error = assertThrows(ExportacionException.class,
                () -> reportes.exportarPdf(admin, vacio, inexistente));
        assertTrue(error.getMessage().contains("PDF"));
        assertThrows(ExportacionException.class, () -> reportes.exportarExcel(admin, vacio,
                carpeta.resolve("no-existe").resolve("x.xlsx")));
    }

    @Test
    void exportarExigePermiso(@TempDir Path carpeta) {
        Usuario tecnico = datos.usuario(Rol.TECNICO);
        Reporte vacio = new Reporte("Reporte vacío", List.of("A"), List.of());
        assertThrows(AccesoDenegadoException.class, () -> reportes.exportarPdf(tecnico, vacio, carpeta.resolve("a.pdf")));
        assertTrue(!Files.exists(carpeta.resolve("a.pdf")));
    }
}
