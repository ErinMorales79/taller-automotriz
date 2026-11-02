package com.taller.escritorio.vista.componentes;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

/** Contenedor blanco con esquinas redondeadas y borde suave; sirve de base para paneles y datos resumidos. */
public class Tarjeta extends JPanel {

    public Tarjeta(LayoutManager disposicion) {
        super(disposicion);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }

    /** Dato resumido: número grande con su descripción y una franja de color. */
    public static Tarjeta estadistica(String titulo, String valor, Color color) {
        Tarjeta tarjeta = new Tarjeta(new BorderLayout(0, 6));
        JLabel etiquetaTitulo = new JLabel(titulo);
        etiquetaTitulo.setForeground(Tema.TEXTO_SUAVE);
        JLabel etiquetaValor = new JLabel(valor);
        etiquetaValor.setFont(etiquetaValor.getFont().deriveFont(Font.BOLD, 28f));
        etiquetaValor.setForeground(color);
        tarjeta.add(etiquetaTitulo, BorderLayout.NORTH);
        tarjeta.add(etiquetaValor, BorderLayout.CENTER);
        return tarjeta;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Tema.TARJETA);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g2.setColor(Tema.BORDE);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g2.dispose();
        super.paintComponent(g);
    }
}
