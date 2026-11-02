package com.taller.escritorio.vista;

import com.formdev.flatlaf.FlatClientProperties;
import com.taller.escritorio.Sesion;
import com.taller.escritorio.controlador.ControladorPrincipal;
import com.taller.escritorio.util.Formato;
import com.taller.escritorio.vista.componentes.Iconos;
import com.taller.escritorio.vista.componentes.Tema;
import com.taller.nucleo.seguridad.Accion;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Ventana principal: menú lateral según el rol y, a la derecha, la pantalla del módulo elegido. */
public final class VentanaPrincipal extends JFrame {

    private record Modulo(String titulo, String icono, Accion permiso, Supplier<JComponent> pantalla) {
    }

    private final JPanel contenido = new JPanel(new BorderLayout());
    private final ButtonGroup grupo = new ButtonGroup();
    private final JPanel menu = new JPanel();
    private final List<JToggleButton> botones = new ArrayList<>();
    private ControladorPrincipal controlador;

    public VentanaPrincipal() {
        super("Taller Automotriz");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        contenido.setBackground(Tema.FONDO);
        setLayout(new BorderLayout());
        add(construirMenu(), BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);
        setMinimumSize(new Dimension(1100, 680));
        setSize(1280, 780);
        setLocationRelativeTo(null);
        if (!botones.isEmpty()) {
            botones.get(0).doClick();
        }
    }

    public void setControlador(ControladorPrincipal controlador) {
        this.controlador = controlador;
    }

    private JPanel construirMenu() {
        menu.setBackground(Tema.MENU_FONDO);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(20, 12, 16, 12));
        menu.setPreferredSize(new Dimension(236, 0));

        JLabel marca = new JLabel("Taller Automotriz", Iconos.de("auto", Color.WHITE, 26), SwingConstants.LEFT);
        marca.setIconTextGap(10);
        marca.setForeground(Color.WHITE);
        marca.setFont(marca.getFont().deriveFont(Font.BOLD, 16f));
        marca.setBorder(BorderFactory.createEmptyBorder(0, 8, 22, 0));
        marca.setAlignmentX(LEFT_ALIGNMENT);
        menu.add(marca);

        for (Modulo modulo : modulos()) {
            if (modulo.permiso() == null || Sesion.puede(modulo.permiso())) {
                menu.add(crearBoton(modulo));
                menu.add(Box.createVerticalStrut(4));
            }
        }
        menu.add(Box.createVerticalGlue());
        menu.add(construirUsuario());
        return menu;
    }

    private List<Modulo> modulos() {
        return List.of(
                new Modulo("Inicio", "casa", null, PanelInicio::new),
                new Modulo("Órdenes", "orden", null, PanelOrdenes::new),
                new Modulo("Clientes", "usuarios", Accion.GESTIONAR_CLIENTES_Y_VEHICULOS, Pantallas::clientes),
                new Modulo("Vehículos", "auto", Accion.GESTIONAR_CLIENTES_Y_VEHICULOS, PanelVehiculos::new),
                new Modulo("Inventario", "inventario", Accion.GESTIONAR_INVENTARIO, PanelInventario::new),
                new Modulo("Reportes", "reportes", Accion.VER_REPORTES, PanelReportes::new),
                new Modulo("Catálogos", "catalogo", Accion.GESTIONAR_CATALOGOS, PanelCatalogos::new),
                new Modulo("Usuarios", "persona", Accion.GESTIONAR_USUARIOS, PanelUsuarios::new),
                new Modulo("Auditoría", "escudo", Accion.VER_AUDITORIA, PanelAuditoria::new));
    }

    private JToggleButton crearBoton(Modulo modulo) {
        JToggleButton boton = new JToggleButton(modulo.titulo(), Iconos.de(modulo.icono(), Tema.MENU_TEXTO));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setIconTextGap(12);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boton.setAlignmentX(LEFT_ALIGNMENT);
        boton.setForeground(Tema.MENU_TEXTO);
        boton.putClientProperty(FlatClientProperties.STYLE,
                "background:#0F172A; selectedBackground:#2563EB; hoverBackground:#1E293B; "
                        + "selectedForeground:#FFFFFF; borderWidth:0; focusWidth:0; arc:10; margin:8,12,8,12; "
                        + "buttonType:toolBarButton");
        boton.addActionListener(e -> mostrar(modulo));
        grupo.add(boton);
        botones.add(boton);
        return boton;
    }

    private JPanel construirUsuario() {
        JLabel nombre = new JLabel(Sesion.usuario().getNombreUsuario());
        nombre.setForeground(Color.WHITE);
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD));
        JLabel rol = new JLabel(Formato.legible(Sesion.usuario().getRol()));
        rol.setForeground(Tema.MENU_TEXTO);
        JPanel datos = new JPanel(new GridLayout(2, 1));
        datos.setOpaque(false);
        datos.add(nombre);
        datos.add(rol);

        javax.swing.JButton clave = new javax.swing.JButton(Iconos.de("candado", Tema.MENU_TEXTO));
        clave.setToolTipText("Cambiar mi contraseña");
        clave.putClientProperty(FlatClientProperties.STYLE, "buttonType:toolBarButton; focusWidth:0");
        clave.addActionListener(e -> controlador.cambiarContrasena());
        javax.swing.JButton salir = new javax.swing.JButton(Iconos.de("salir", Tema.MENU_TEXTO));
        salir.setToolTipText("Cerrar sesión");
        salir.putClientProperty(FlatClientProperties.STYLE, "buttonType:toolBarButton; focusWidth:0");
        salir.addActionListener(e -> controlador.cerrarSesion());

        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(Tema.MENU_ACTIVO);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 8));
        panel.add(new JLabel(Iconos.de("persona", Color.WHITE, 24)), BorderLayout.WEST);
        panel.add(datos, BorderLayout.CENTER);
        JPanel botones = new JPanel(new GridLayout(1, 2));
        botones.setOpaque(false);
        botones.add(clave);
        botones.add(salir);
        panel.add(botones, BorderLayout.EAST);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        return panel;
    }

    private void mostrar(Modulo modulo) {
        contenido.removeAll();
        contenido.add(modulo.pantalla().get(), BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();
    }
}
