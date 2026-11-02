package com.taller.escritorio.vista;

import com.formdev.flatlaf.FlatClientProperties;
import com.taller.escritorio.controlador.ControladorReportes;
import com.taller.escritorio.controlador.TipoReporte;
import com.taller.escritorio.vista.componentes.Boton;
import com.taller.escritorio.vista.componentes.Encabezado;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.escritorio.vista.componentes.Tarjeta;
import com.taller.escritorio.vista.componentes.Tema;
import com.taller.nucleo.entidad.Vehiculo;
import com.taller.nucleo.reporte.Reporte;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Reportes en pantalla con filtros; desde aquí se exportan a PDF o Excel. */
public final class PanelReportes extends JPanel {

    private final JComboBox<TipoReporte> tipo = new JComboBox<>(TipoReporte.values());
    private final JTextField desde = campo("dd/mm/aaaa", 9);
    private final JTextField hasta = campo("dd/mm/aaaa", 9);
    private final JTextField limite = campo("10", 4);
    private final JComboBox<Vehiculo> vehiculo = new JComboBox<>();
    private final JLabel etiquetaDesde = etiqueta("Desde");
    private final JLabel etiquetaHasta = etiqueta("Hasta");
    private final JLabel etiquetaLimite = etiqueta("Máx. repuestos");
    private final JLabel etiquetaVehiculo = etiqueta("Vehículo");
    private final JLabel resumen = new JLabel("Elige un reporte y pulsa Generar.");
    private final Tarjeta resultado = new Tarjeta(new BorderLayout());
    private final ControladorReportes controlador = new ControladorReportes(this);
    private int filas;

    public PanelReportes() {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(new Encabezado("Reportes", "Información real del taller, lista para exportar"), BorderLayout.NORTH);

        JButton generar = Boton.primario("Generar", "reportes");
        JButton pdf = Boton.secundario("PDF", "descargar");
        JButton excel = Boton.secundario("Excel", "descargar");
        generar.addActionListener(e -> controlador.generar((TipoReporte) tipo.getSelectedItem(), desde.getText(),
                hasta.getText(), limite.getText(), (Vehiculo) vehiculo.getSelectedItem()));
        pdf.addActionListener(e -> controlador.exportar(true));
        excel.addActionListener(e -> controlador.exportar(false));
        tipo.addActionListener(e -> ajustarFiltros());
        vehiculo.setPrototypeDisplayValue(null);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        for (java.awt.Component componente : new java.awt.Component[]{tipo, etiquetaDesde, desde, etiquetaHasta, hasta,
                etiquetaLimite, limite, etiquetaVehiculo, vehiculo, generar, pdf, excel}) {
            filtros.add(componente);
        }
        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);
        centro.add(filtros, BorderLayout.NORTH);
        resumen.setForeground(Tema.TEXTO_SUAVE);
        resultado.add(resumen, BorderLayout.CENTER);
        centro.add(resultado, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
        ajustarFiltros();
        controlador.cargarVehiculos();
    }

    private static JTextField campo(String ayuda, int columnas) {
        JTextField campo = new JTextField(columnas);
        campo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, ayuda);
        return campo;
    }

    private static JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(Tema.TEXTO_SUAVE);
        return etiqueta;
    }

    private void ajustarFiltros() {
        TipoReporte elegido = (TipoReporte) tipo.getSelectedItem();
        for (java.awt.Component c : List.of(etiquetaDesde, desde, etiquetaHasta, hasta)) {
            c.setVisible(elegido.usaFechas());
        }
        for (java.awt.Component c : List.of(etiquetaLimite, limite)) {
            c.setVisible(elegido.usaLimite());
        }
        for (java.awt.Component c : List.of(etiquetaVehiculo, vehiculo)) {
            c.setVisible(elegido.usaVehiculo());
        }
    }

    public void mostrarVehiculos(List<Vehiculo> vehiculos) {
        vehiculo.removeAllItems();
        vehiculos.forEach(vehiculo::addItem);
        vehiculo.setSelectedIndex(-1);
    }

    public void mostrar(Reporte reporte) {
        List<Columna<List<Object>>> columnas = new ArrayList<>();
        for (int i = 0; i < reporte.columnas().size(); i++) {
            int indice = i;
            columnas.add(new Columna<>(reporte.columnas().get(i), fila -> fila.get(indice)));
        }
        TablaDatos<List<Object>> tabla = new TablaDatos<>(columnas);
        tabla.setDatos(reporte.filas());
        filas = reporte.filas().size();
        resumen.setText(reporte.titulo() + "  -  " + filas + (filas == 1 ? " fila" : " filas")
                + (reporte.estaVacio() ? "  (sin datos para los filtros elegidos)" : ""));
        resultado.removeAll();
        resultado.add(resumen, BorderLayout.NORTH);
        resultado.add(tabla, BorderLayout.CENTER);
        resultado.revalidate();
        resultado.repaint();
    }

    public int filasMostradas() {
        return filas;
    }

    /** Pregunta dónde guardar el archivo; devuelve null si se cancela. */
    public Path elegirDestino(String extension, String nombreSugerido) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar reporte");
        selector.setFileFilter(new FileNameExtensionFilter(extension.toUpperCase() + " (*." + extension + ")", extension));
        selector.setSelectedFile(new java.io.File(nombreSugerido.replaceAll("[^\\p{L}\\p{N} ]", "").trim().replace(' ', '_')
                + "." + extension));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        Path ruta = selector.getSelectedFile().toPath();
        return ruta.toString().toLowerCase().endsWith("." + extension) ? ruta : Path.of(ruta + "." + extension);
    }
}
