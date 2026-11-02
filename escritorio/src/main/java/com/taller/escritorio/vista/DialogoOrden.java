package com.taller.escritorio.vista;

import com.taller.escritorio.controlador.ControladorOrden;
import com.taller.escritorio.controlador.DetalleOrden;
import com.taller.escritorio.util.Formato;
import com.taller.escritorio.vista.componentes.Iconos;
import com.taller.escritorio.vista.componentes.Tema;
import com.taller.escritorio.vista.orden.Pestana;
import com.taller.escritorio.vista.orden.PestanaDiagnostico;
import com.taller.escritorio.vista.orden.PestanaHistorial;
import com.taller.escritorio.vista.orden.PestanaPagos;
import com.taller.escritorio.vista.orden.PestanaPresupuestos;
import com.taller.escritorio.vista.orden.PestanaRepuestos;
import com.taller.escritorio.vista.orden.PestanaResumen;
import com.taller.escritorio.vista.orden.PestanaTecnicos;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

/** Detalle de una orden en pestañas: resumen, diagnóstico, presupuestos, técnicos, repuestos, pagos e historial. */
public final class DialogoOrden extends JDialog {

    private final JLabel titulo = new JLabel();
    private final JLabel subtitulo = new JLabel();
    private final List<Pestana> pestanas = new ArrayList<>();
    private final ControladorOrden controlador;

    public DialogoOrden(Window padre, int idOrden) {
        super(padre, "Orden #" + idOrden, ModalityType.APPLICATION_MODAL);
        controlador = new ControladorOrden(this, idOrden);

        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        subtitulo.setForeground(Tema.TEXTO_SUAVE);
        JPanel cabecera = new JPanel(new BorderLayout(0, 2));
        cabecera.setBorder(BorderFactory.createEmptyBorder(16, 24, 12, 24));
        cabecera.add(titulo, BorderLayout.NORTH);
        cabecera.add(subtitulo, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.putClientProperty("JTabbedPane.tabType", "underlined");
        agregar(tabs, "Resumen", "orden", new PestanaResumen(controlador));
        agregar(tabs, "Diagnóstico", "ojo", new PestanaDiagnostico(controlador));
        agregar(tabs, "Presupuestos", "cartera", new PestanaPresupuestos(controlador));
        agregar(tabs, "Técnicos", "usuarios", new PestanaTecnicos(controlador));
        agregar(tabs, "Repuestos usados", "inventario", new PestanaRepuestos(controlador));
        agregar(tabs, "Pagos", "cartera", new PestanaPagos(controlador));
        agregar(tabs, "Historial", "refrescar", new PestanaHistorial());

        setLayout(new BorderLayout());
        add(cabecera, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setPreferredSize(new Dimension(980, 680));
        pack();
        setLocationRelativeTo(padre);
        controlador.cargar();
    }

    private void agregar(JTabbedPane tabs, String nombre, String icono, Pestana pestana) {
        pestanas.add(pestana);
        tabs.addTab(nombre, Iconos.de(icono, Tema.TEXTO_SUAVE), pestana);
    }

    public void mostrar(DetalleOrden detalle) {
        var vehiculo = detalle.orden().getVehiculo();
        titulo.setText("Orden #" + detalle.orden().getId() + "  -  " + vehiculo.getPlaca());
        subtitulo.setText(Formato.legible(detalle.estado()) + "  |  " + vehiculo.getPropietario().getNombre()
                + "  |  " + detalle.orden().getTipoAtencion().getNombre());
        pestanas.forEach(pestana -> pestana.mostrar(detalle));
    }
}
