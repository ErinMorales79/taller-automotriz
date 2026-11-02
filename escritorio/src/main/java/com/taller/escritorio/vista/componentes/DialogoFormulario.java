package com.taller.escritorio.vista.componentes;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.function.Consumer;

/** Ventana modal estándar con un formulario y los botones Guardar y Cancelar. */
public final class DialogoFormulario extends JDialog {

    public DialogoFormulario(Window padre, String titulo, JComponent contenido, String textoGuardar,
                             Consumer<DialogoFormulario> alGuardar) {
        super(padre, titulo, ModalityType.APPLICATION_MODAL);
        JPanel cuerpo = new JPanel(new BorderLayout(0, 18));
        cuerpo.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        cuerpo.add(contenido, BorderLayout.CENTER);

        JButton guardar = Boton.primario(textoGuardar, "check");
        JButton cancelar = Boton.secundario("Cancelar", "cerrar");
        guardar.addActionListener(e -> alGuardar.accept(this));
        cancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(cancelar);
        botones.add(guardar);
        cuerpo.add(botones, BorderLayout.SOUTH);

        setContentPane(cuerpo);
        JRootPane raiz = getRootPane();
        raiz.setDefaultButton(guardar);
        raiz.registerKeyboardAction(e -> dispose(), javax.swing.KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        setResizable(false);
        pack();
        setLocationRelativeTo(padre);
    }

    public void cerrar() {
        dispose();
    }
}
