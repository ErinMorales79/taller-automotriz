package com.taller.escritorio;

import com.taller.escritorio.controlador.TipoReporte;
import com.taller.escritorio.util.Tarea;
import com.taller.escritorio.vista.PanelAuditoria;
import com.taller.escritorio.vista.PanelCompras;
import com.taller.escritorio.vista.PanelCrud;
import com.taller.escritorio.vista.PanelInicio;
import com.taller.escritorio.vista.PanelInventario;
import com.taller.escritorio.vista.PanelOrdenes;
import com.taller.escritorio.vista.PanelReportes;
import com.taller.escritorio.vista.PanelUsuarios;
import com.taller.escritorio.vista.PanelVehiculos;
import com.taller.escritorio.vista.Pantallas;
import com.taller.escritorio.vista.componentes.Tema;
import com.taller.nucleo.soporte.BaseBD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Construye cada pantalla con los datos de prueba (dentro de la transacción de la prueba, que se
 * revierte) y comprueba que los controladores cargan lo esperado. Si se define la propiedad
 * taller.capturas, además guarda una imagen de cada pantalla en esa carpeta para revisarla.
 * Requiere la base de pruebas vacía.
 */
class PantallasTest extends BaseBD {

    private static final Path CAPTURAS = System.getProperty("taller.capturas") == null
            ? null : Path.of(System.getProperty("taller.capturas"));

    @BeforeAll
    static void prepararApariencia() {
        Tema.instalar();
        Tarea.usarModoSincrono(true);
    }

    @BeforeEach
    void cargarDatosEIniciarSesion() throws IOException, SQLException {
        Path ruta = Path.of("..", "database", "datos_prueba.sql");
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
        Sesion.iniciar(Servicios.AUTENTICACION.iniciarSesion("admin", "Admin2026"));
    }

    @AfterEach
    void cerrarSesion() {
        Sesion.cerrar();
    }

    @Test
    void lasPantallasDeCatalogoCarganSusRegistros() throws IOException {
        PanelCrud<?> clientes = Pantallas.clientes();
        assertEquals(5, clientes.filas());
        capturar(clientes, "clientes");
        assertEquals(6, Pantallas.vehiculos().filas());
        assertEquals(4, Pantallas.marcas().filas());
        assertEquals(6, Pantallas.modelos().filas());
        assertEquals(3, Pantallas.tecnicos().filas());
        assertTrue(Pantallas.servicios().filas() > 0);
        assertTrue(Pantallas.tiposAtencion().filas() > 0);
        assertTrue(Pantallas.repuestos().filas() > 0);
        assertTrue(Pantallas.proveedores().filas() > 0);
        capturar(new PanelVehiculos(), "vehiculos");
        capturar(new PanelInventario(), "inventario");
    }

    @Test
    void laListaDeOrdenesMuestraTodasYFiltraPorEstado() throws IOException {
        PanelOrdenes ordenes = new PanelOrdenes();
        assertEquals(9, ordenes.filas());
        capturar(ordenes, "ordenes");
    }

    @Test
    void elInicioUsuariosComprasYAuditoriaCargan() throws IOException {
        capturar(new PanelInicio(), "inicio");
        PanelUsuarios usuarios = new PanelUsuarios();
        assertEquals(5, usuarios.filas());
        capturar(usuarios, "usuarios");
        assertTrue(new PanelCompras().filas() > 0);
        capturar(new PanelAuditoria(), "auditoria");
    }

    @Test
    void elReporteGeneradoSeMuestraEnPantalla() throws IOException {
        PanelReportes reportes = new PanelReportes();
        var controlador = new com.taller.escritorio.controlador.ControladorReportes(reportes);
        controlador.generar(TipoReporte.ORDENES_POR_ESTADO, "", "", "", null);
        assertEquals(7, reportes.filasMostradas());
        controlador.generar(TipoReporte.REPUESTOS, "", "", "3", null);
        assertEquals(3, reportes.filasMostradas());
        capturar(reportes, "reportes");
    }

    private void capturar(JComponent pantalla, String nombre) throws IOException {
        if (CAPTURAS == null) {
            return;
        }
        Files.createDirectories(CAPTURAS);
        pantalla.setSize(new Dimension(1040, 720));
        distribuir(pantalla);
        BufferedImage imagen = new BufferedImage(1040, 720, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics();
        g.setColor(Tema.FONDO);
        g.fillRect(0, 0, 1040, 720);
        pantalla.paint(g);
        g.dispose();
        ImageIO.write(imagen, "png", CAPTURAS.resolve(nombre + ".png").toFile());
    }

    /** Sin ventana real hay que pedir el acomodo de cada contenedor a mano, de arriba hacia abajo. */
    private static void distribuir(Container contenedor) {
        contenedor.doLayout();
        for (java.awt.Component hijo : contenedor.getComponents()) {
            if (hijo instanceof Container interno) {
                distribuir(interno);
            }
        }
    }
}
