package com.taller.escritorio.vista.componentes;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

/** Título de pantalla con subtítulo y, a la derecha, los botones de acción. */
public final class Encabezado extends JPanel {

    private final JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

    public Encabezado(String titulo, String subtitulo) {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel etiquetaTitulo = new JLabel(titulo);
        etiquetaTitulo.setFont(etiquetaTitulo.getFont().deriveFont(Font.BOLD, 24f));
        JLabel etiquetaSubtitulo = new JLabel(subtitulo);
        etiquetaSubtitulo.setForeground(Tema.TEXTO_SUAVE);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(etiquetaTitulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(etiquetaSubtitulo);
        add(textos, BorderLayout.WEST);

        acciones.setOpaque(false);
        add(acciones, BorderLayout.EAST);
    }

    public void agregarAccion(JComponent boton) {
        acciones.add(boton);
    }
}
