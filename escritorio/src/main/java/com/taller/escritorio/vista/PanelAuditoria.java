package com.taller.escritorio.vista;

import com.taller.escritorio.controlador.ControladorAuditoria;
import com.taller.escritorio.vista.componentes.Encabezado;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.escritorio.vista.componentes.Tema;
import com.taller.nucleo.entidad.Auditoria;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/** Registro de auditoría: quién hizo qué y cuándo, consultado por la tabla afectada. */
public final class PanelAuditoria extends JPanel {

    private static final String[] TABLAS = {"orden_servicio", "presupuesto", "pago", "diagnostico", "asignacion",
            "consumo_repuesto", "compra", "repuesto", "cliente", "vehiculo", "marca", "modelo", "tecnico", "servicio",
            "tipo_atencion", "proveedor", "usuario"};

    private final TablaDatos<Auditoria> tabla = new TablaDatos<>(List.of(
            new Columna<>("Fecha", Auditoria::getFecha),
            new Columna<>("Usuario", a -> a.getUsuario() == null ? "-" : a.getUsuario().getNombreUsuario()),
            new Columna<>("Acción", Auditoria::getAccion), new Columna<>("Tabla", Auditoria::getTablaAfectada),
            new Columna<>("Registro", Auditoria::getIdRegistro)));
    private final JComboBox<String> tablas = new JComboBox<>(TABLAS);
    private final ControladorAuditoria controlador = new ControladorAuditoria(this);

    public PanelAuditoria() {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(new Encabezado("Auditoría", "Quién hizo qué y cuándo"), BorderLayout.NORTH);
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtro.setOpaque(false);
        JLabel etiqueta = new JLabel("Tabla afectada");
        etiqueta.setForeground(Tema.TEXTO_SUAVE);
        filtro.add(etiqueta);
        filtro.add(tablas);
        tablas.addActionListener(e -> controlador.cargar());
        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);
        centro.add(filtro, BorderLayout.NORTH);
        centro.add(tabla, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
        controlador.cargar();
    }

    public String tablaElegida() {
        return (String) tablas.getSelectedItem();
    }

    public void mostrar(List<Auditoria> registros) {
        tabla.setDatos(registros);
    }
}
