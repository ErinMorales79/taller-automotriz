package com.taller.escritorio.vista;

import com.taller.escritorio.vista.componentes.Formulario;
import com.taller.escritorio.vista.componentes.Tema;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Optional;

/** Pide las credenciales de un administrador para autorizar una excepción a las reglas. */
public final class DialogoAutorizacion {

    public record Credenciales(String usuario, String contrasena) {
    }

    private DialogoAutorizacion() {
    }

    public static Optional<Credenciales> pedir(Component padre, String motivo) {
        JLabel texto = new JLabel("<html><body style='width:300px'>" + motivo
                + "<br><br>Un administrador debe autorizarlo con su usuario y contraseña.</body></html>");
        texto.setForeground(Tema.TEXTO_SUAVE);
        Formulario formulario = new Formulario();
        JTextField usuario = formulario.agregarTexto("Usuario del administrador");
        JPasswordField contrasena = formulario.agregar("Contraseña", new JPasswordField(26));
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.add(texto, BorderLayout.NORTH);
        panel.add(formulario, BorderLayout.CENTER);
        int respuesta = JOptionPane.showConfirmDialog(padre, panel, "Autorización de administrador",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (respuesta != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }
        return Optional.of(new Credenciales(usuario.getText(), new String(contrasena.getPassword())));
    }
}
