package com.taller.escritorio.vista;

import com.formdev.flatlaf.FlatClientProperties;
import com.taller.escritorio.controlador.ControladorLogin;
import com.taller.escritorio.vista.componentes.Boton;
import com.taller.escritorio.vista.componentes.Formulario;
import com.taller.escritorio.vista.componentes.Iconos;
import com.taller.escritorio.vista.componentes.Tema;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

/** Pantalla de acceso: marca del taller a la izquierda y formulario de ingreso a la derecha. */
public final class VentanaLogin extends JFrame {

    private final JTextField usuario = new JTextField(22);
    private final JPasswordField contrasena = new JPasswordField(22);
    private final JLabel mensaje = new JLabel(" ");
    private final JButton ingresar = Boton.primario("Ingresar", "candado");
    private ControladorLogin controlador;

    public VentanaLogin() {
        super("Taller Automotriz - Iniciar sesión");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel contenido = new JPanel(new GridLayout(1, 2));
        contenido.add(construirMarca());
        contenido.add(construirFormulario());
        setContentPane(contenido);
        setMinimumSize(new Dimension(820, 480));
        pack();
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(ingresar);
    }

    public void setControlador(ControladorLogin controlador) {
        this.controlador = controlador;
    }

    public String usuarioEscrito() {
        return usuario.getText();
    }

    public String contrasenaEscrita() {
        return new String(contrasena.getPassword());
    }

    public void mostrarError(String texto) {
        mensaje.setText(texto);
        contrasena.setText("");
        contrasena.requestFocusInWindow();
    }

    private JPanel construirMarca() {
        JPanel panel = new JPanel();
        panel.setBackground(Tema.MENU_FONDO);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(60, 48, 40, 48));
        JLabel icono = new JLabel(Iconos.de("auto", Color.WHITE, 56));
        JLabel nombre = new JLabel("Taller Automotriz");
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 30f));
        nombre.setForeground(Color.WHITE);
        JLabel lema = new JLabel("<html>Órdenes de servicio, presupuestos,<br>inventario y entrega de vehículos<br>en un solo lugar.</html>");
        lema.setForeground(Tema.MENU_TEXTO);
        panel.add(icono);
        panel.add(Box.createVerticalStrut(20));
        panel.add(nombre);
        panel.add(Box.createVerticalStrut(12));
        panel.add(lema);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel construirFormulario() {
        JPanel lado = new JPanel(new BorderLayout());
        lado.setBackground(Color.WHITE);
        lado.setBorder(BorderFactory.createEmptyBorder(56, 52, 40, 52));

        JLabel titulo = new JLabel("Bienvenido");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 26f));
        JLabel subtitulo = new JLabel("Ingresa con tu usuario y contraseña");
        subtitulo.setForeground(Tema.TEXTO_SUAVE);
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));
        textos.add(titulo);
        textos.add(subtitulo);

        usuario.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Usuario");
        usuario.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Iconos.de("persona", Tema.TEXTO_SUAVE));
        contrasena.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Contraseña");
        contrasena.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Iconos.de("candado", Tema.TEXTO_SUAVE));
        contrasena.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        Formulario formulario = new Formulario();
        formulario.agregar("Usuario", usuario);
        formulario.agregar("Contraseña", contrasena);

        mensaje.setForeground(Tema.PELIGRO);
        ingresar.putClientProperty(FlatClientProperties.STYLE_CLASS, "ingresar");
        ingresar.addActionListener(e -> controlador.iniciar(usuarioEscrito(), contrasenaEscrita()));
        JPanel pie = new JPanel(new BorderLayout(0, 12));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        pie.add(mensaje, BorderLayout.NORTH);
        pie.add(ingresar, BorderLayout.CENTER);

        lado.add(textos, BorderLayout.NORTH);
        lado.add(formulario, BorderLayout.CENTER);
        lado.add(pie, BorderLayout.SOUTH);
        return lado;
    }
}
