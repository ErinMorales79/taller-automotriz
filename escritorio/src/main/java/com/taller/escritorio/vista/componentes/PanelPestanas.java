package com.taller.escritorio.vista.componentes;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

/** Agrupa varias pantallas relacionadas en pestañas (por ejemplo, vehículos, marcas y modelos). */
public class PanelPestanas extends JPanel {

    private final JTabbedPane pestanas = new JTabbedPane();

    public PanelPestanas() {
        super(new BorderLayout());
        setOpaque(false);
        pestanas.putClientProperty("JTabbedPane.tabType", "underlined");
        pestanas.putClientProperty("JTabbedPane.tabAreaInsets", new java.awt.Insets(8, 28, 0, 0));
        add(pestanas, BorderLayout.CENTER);
    }

    protected final void agregar(String titulo, String icono, JComponent contenido) {
        contenido.setBorder(contenido.getBorder() == null ? BorderFactory.createEmptyBorder() : contenido.getBorder());
        pestanas.addTab(titulo, Iconos.de(icono, Tema.TEXTO_SUAVE), contenido);
    }
}
