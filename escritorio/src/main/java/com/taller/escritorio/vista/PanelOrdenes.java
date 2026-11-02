package com.taller.escritorio.vista;

import com.formdev.flatlaf.FlatClientProperties;
import com.taller.escritorio.Sesion;
import com.taller.escritorio.controlador.ControladorOrdenes;
import com.taller.escritorio.util.Formato;
import com.taller.escritorio.vista.componentes.Boton;
import com.taller.escritorio.vista.componentes.Encabezado;
import com.taller.escritorio.vista.componentes.Iconos;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.escritorio.vista.componentes.Tema;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.seguridad.Accion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/** Lista de órdenes con filtros por estado y texto (placa o cliente). Doble clic abre el detalle. */
public final class PanelOrdenes extends JPanel {

    private static final String TODOS = "Todos los estados";
    private static final String[] ESTADOS = {"RECIBIDA", "EN_DIAGNOSTICO", "ESPERANDO_APROBACION", "EN_REPARACION",
            "FINALIZADA", "ENTREGADA", "CANCELADA"};

    private final TablaDatos<OrdenServicio> tabla = new TablaDatos<>(columnas());
    private final JComboBox<String> estado = new JComboBox<>();
    private final JTextField texto = new JTextField(24);
    private final JLabel total = new JLabel();
    private final ControladorOrdenes controlador = new ControladorOrdenes(this);

    public PanelOrdenes() {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        Encabezado encabezado = new Encabezado("Órdenes de servicio", "Seguimiento de cada vehículo en el taller");
        JButton abrir = Boton.secundario("Ver detalle", "ojo");
        abrir.addActionListener(e -> controlador.abrir(tabla.seleccionado()));
        encabezado.agregarAccion(abrir);
        if (Sesion.puede(Accion.CREAR_ORDEN)) {
            JButton nueva = Boton.primario("Nueva orden", "mas");
            nueva.addActionListener(e -> controlador.nueva());
            encabezado.agregarAccion(nueva);
        }
        tabla.alDobleClic(controlador::abrir);

        estado.addItem(TODOS);
        for (String nombre : ESTADOS) {
            estado.addItem(Formato.legible(nombre));
        }
        texto.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Placa o cliente...");
        texto.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Iconos.de("buscar", Tema.TEXTO_SUAVE));
        texto.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        estado.addActionListener(e -> controlador.cargar());
        texto.addActionListener(e -> controlador.cargar());
        JButton buscar = Boton.secundario("Buscar", "buscar");
        buscar.addActionListener(e -> controlador.cargar());
        total.setForeground(Tema.TEXTO_SUAVE);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        filtros.add(texto);
        filtros.add(estado);
        filtros.add(buscar);
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        barra.add(filtros, BorderLayout.WEST);
        barra.add(total, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(barra, BorderLayout.NORTH);
        centro.add(tabla, BorderLayout.CENTER);
        add(encabezado, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        controlador.cargar();
    }

    public static List<Columna<OrdenServicio>> columnas() {
        return List.of(
                new Columna<>("N.º", OrdenServicio::getId),
                new Columna<>("Ingreso", OrdenServicio::getFechaIngreso),
                new Columna<>("Placa", o -> o.getVehiculo().getPlaca()),
                new Columna<>("Cliente", o -> o.getVehiculo().getPropietario().getNombre()),
                new Columna<>("Tipo", o -> o.getTipoAtencion().getNombre()),
                new Columna<>("Prioridad", OrdenServicio::getPrioridad),
                new Columna<>("Estado", o -> o.getEstado().getNombre()),
                new Columna<>("Antigüedad", PanelOrdenes::antiguedad));
    }

    private static String antiguedad(OrdenServicio orden) {
        if (!orden.estaActiva()) {
            return "-";
        }
        long dias = Duration.between(orden.getFechaIngreso(), LocalDateTime.now()).toDays();
        return dias == 1 ? "1 día" : dias + " días";
    }

    /** Nombre técnico del estado elegido, o null si se piden todos. */
    public String estadoElegido() {
        int indice = estado.getSelectedIndex();
        return indice <= 0 ? null : ESTADOS[indice - 1];
    }

    public String textoBuscado() {
        return texto.getText();
    }

    public void mostrar(List<OrdenServicio> ordenes) {
        tabla.setDatos(ordenes);
        total.setText(ordenes.size() + (ordenes.size() == 1 ? " orden" : " órdenes"));
    }

    public Window ventana() {
        return SwingUtilities.getWindowAncestor(this);
    }

    public int filas() {
        return tabla.cantidadFilas();
    }
}
