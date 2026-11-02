package com.taller.escritorio.vista.componentes;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Formulario de una columna: cada campo lleva su etiqueta encima, igual en todas las pantallas. */
public final class Formulario extends JPanel {

    private int fila;

    public Formulario() {
        super(new GridBagLayout());
        setOpaque(false);
    }

    public <C extends JComponent> C agregar(String etiqueta, C campo) {
        JLabel texto = new JLabel(etiqueta);
        texto.setForeground(Tema.TEXTO_SUAVE);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = fila++;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(fila == 1 ? 0 : 10, 0, 3, 0);
        add(texto, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = fila++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(campo, c);
        return campo;
    }

    public JTextField agregarTexto(String etiqueta) {
        JTextField campo = new JTextField(26);
        return agregar(etiqueta, campo);
    }
}
