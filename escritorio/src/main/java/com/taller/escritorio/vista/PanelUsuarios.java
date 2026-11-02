package com.taller.escritorio.vista;

import com.taller.escritorio.controlador.ControladorUsuarios;
import com.taller.escritorio.vista.componentes.Boton;
import com.taller.escritorio.vista.componentes.Encabezado;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.nucleo.entidad.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Window;
import java.util.List;

/** Usuarios del sistema con su rol; solo para el administrador. */
public final class PanelUsuarios extends JPanel {

    private final TablaDatos<Usuario> tabla = new TablaDatos<>(List.of(
            new Columna<>("Usuario", Usuario::getNombreUsuario), new Columna<>("Rol", Usuario::getRol),
            new Columna<>("Técnico ligado", u -> u.getTecnico() == null ? null : u.getTecnico().getNombre()),
            new Columna<>("Activo", Usuario::isActivo)));
    private final ControladorUsuarios controlador = new ControladorUsuarios(this);

    public PanelUsuarios() {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        Encabezado encabezado = new Encabezado("Usuarios", "Quién puede entrar al sistema y con qué rol");
        JButton desactivar = Boton.peligro("Desactivar", "cerrar");
        JButton rol = Boton.secundario("Cambiar rol", "editar");
        JButton nuevo = Boton.primario("Nuevo usuario", "mas");
        desactivar.addActionListener(e -> controlador.desactivar(tabla.seleccionado()));
        rol.addActionListener(e -> controlador.cambiarRol(tabla.seleccionado()));
        nuevo.addActionListener(e -> controlador.nuevo());
        encabezado.agregarAccion(desactivar);
        encabezado.agregarAccion(rol);
        encabezado.agregarAccion(nuevo);
        add(encabezado, BorderLayout.NORTH);
        add(tabla, BorderLayout.CENTER);
        controlador.cargar();
    }

    public void mostrar(List<Usuario> usuarios) {
        tabla.setDatos(usuarios);
    }

    public Window ventana() {
        return SwingUtilities.getWindowAncestor(this);
    }

    public int filas() {
        return tabla.cantidadFilas();
    }
}
