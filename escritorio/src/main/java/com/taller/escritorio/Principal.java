package com.taller.escritorio;

import com.taller.escritorio.controlador.ControladorLogin;
import com.taller.escritorio.vista.VentanaLogin;
import com.taller.escritorio.vista.componentes.Tema;

import javax.swing.SwingUtilities;

public class Principal {

    public static void main(String[] argumentos) {
        Tema.instalar();
        SwingUtilities.invokeLater(() -> {
            VentanaLogin ventana = new VentanaLogin();
            ventana.setControlador(new ControladorLogin(ventana));
            ventana.setVisible(true);
        });
    }
}
