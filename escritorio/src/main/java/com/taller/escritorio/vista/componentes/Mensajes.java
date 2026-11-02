package com.taller.escritorio.vista.componentes;

import javax.swing.JOptionPane;
import java.awt.Component;

/** Mensajes al usuario, siempre en español y con el mismo estilo. */
public final class Mensajes {

    private Mensajes() {
    }

    public static void informar(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Una regla de negocio o una validación no se cumplió: el usuario puede corregirlo. */
    public static void avisar(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component padre, String pregunta) {
        return JOptionPane.showConfirmDialog(padre, pregunta, "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
