package com.taller.escritorio.vista.orden;

import com.taller.escritorio.controlador.DetalleOrden;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/** Base de las pestañas del detalle de una orden: cada una muestra su parte del DetalleOrden. */
public abstract class Pestana extends JPanel {

    protected Pestana() {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
    }

    public abstract void mostrar(DetalleOrden detalle);

    /** Fila de botones de acción, alineada a la izquierda. */
    protected static JPanel barra(JComponent... botones) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setOpaque(false);
        for (JComponent boton : botones) {
            barra.add(boton);
        }
        return barra;
    }

    protected static void mostrarSi(JButton boton, boolean visible) {
        boton.setVisible(visible);
    }
}
