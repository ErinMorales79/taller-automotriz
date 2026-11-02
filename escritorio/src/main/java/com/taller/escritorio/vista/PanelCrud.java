package com.taller.escritorio.vista;

import com.taller.escritorio.Sesion;
import com.taller.escritorio.controlador.ControladorCrud;
import com.taller.escritorio.vista.componentes.Boton;
import com.taller.escritorio.vista.componentes.ConfiguracionCrud;
import com.taller.escritorio.vista.componentes.Encabezado;
import com.taller.escritorio.vista.componentes.Iconos;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.Tema;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Window;
import java.util.List;

/** Pantalla estándar de un catálogo: búsqueda, tabla y botones para crear, editar y eliminar. */
public final class PanelCrud<T> extends JPanel {

    private final TablaDatos<T> tabla;
    private final JLabel total = new JLabel();
    private final JButton editar;
    private final JButton eliminar;

    public PanelCrud(ConfiguracionCrud<T> configuracion) {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        ControladorCrud<T> controlador = new ControladorCrud<>(this, configuracion);
        tabla = new TablaDatos<>(configuracion.columnas());

        Encabezado encabezado = new Encabezado(configuracion.titulo(), configuracion.subtitulo());
        JButton nuevo = Boton.primario("Nuevo " + configuracion.singular(), "mas");
        editar = Boton.secundario("Editar", "editar");
        eliminar = Boton.peligro("Eliminar", "borrar");
        JButton refrescar = Boton.secundario("", "refrescar");
        refrescar.setToolTipText("Actualizar la lista");
        nuevo.addActionListener(e -> controlador.nuevo());
        editar.addActionListener(e -> controlador.editar(tabla.seleccionado()));
        eliminar.addActionListener(e -> controlador.eliminar(tabla.seleccionado()));
        refrescar.addActionListener(e -> controlador.cargar());
        encabezado.agregarAccion(refrescar);
        if (Sesion.puede(configuracion.permiso())) {
            encabezado.agregarAccion(eliminar);
            encabezado.agregarAccion(editar);
            encabezado.agregarAccion(nuevo);
            tabla.alDobleClic(controlador::editar);
        }
        tabla.alElegir(this::actualizarBotones);
        actualizarBotones();

        JTextField buscar = new JTextField(28);
        buscar.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar...");
        buscar.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Iconos.de("buscar", Tema.TEXTO_SUAVE));
        buscar.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        buscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                tabla.filtrar(buscar.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                tabla.filtrar(buscar.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                tabla.filtrar(buscar.getText());
            }
        });
        total.setForeground(Tema.TEXTO_SUAVE);
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        barra.add(buscar, BorderLayout.WEST);
        barra.add(total, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(barra, BorderLayout.NORTH);
        centro.add(tabla, BorderLayout.CENTER);
        add(encabezado, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        controlador.cargar();
    }

    public void mostrar(List<T> datos) {
        tabla.setDatos(datos);
        total.setText(datos.size() + (datos.size() == 1 ? " registro" : " registros"));
        actualizarBotones();
    }

    public T seleccionado() {
        return tabla.seleccionado();
    }

    public Window ventana() {
        return SwingUtilities.getWindowAncestor(this);
    }

    public int filas() {
        return tabla.cantidadFilas();
    }

    private void actualizarBotones() {
        boolean hayFila = tabla.seleccionado() != null;
        editar.setEnabled(hayFila);
        eliminar.setEnabled(hayFila);
    }
}
