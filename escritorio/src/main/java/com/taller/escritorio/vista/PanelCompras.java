package com.taller.escritorio.vista;

import com.taller.escritorio.Sesion;
import com.taller.escritorio.controlador.ControladorCompras;
import com.taller.escritorio.vista.componentes.Boton;
import com.taller.escritorio.vista.componentes.Encabezado;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.nucleo.entidad.Compra;
import com.taller.nucleo.seguridad.Accion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Window;
import java.util.List;

/** Historial de compras de repuestos y acceso para registrar una nueva (suma al stock). */
public final class PanelCompras extends JPanel {

    private final TablaDatos<Compra> tabla = new TablaDatos<>(List.of(
            new Columna<>("N.º", Compra::getId),
            new Columna<>("Fecha", Compra::getFecha),
            new Columna<>("Proveedor", c -> c.getProveedor().getNombre()),
            new Columna<>("Registró", c -> c.getUsuario().getNombreUsuario()),
            new Columna<>("Artículos", c -> c.getDetalles().size()),
            new Columna<>("Total", Compra::total)));

    public PanelCompras() {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        ControladorCompras controlador = new ControladorCompras(this);
        Encabezado encabezado = new Encabezado("Compras", "Entradas de repuestos al inventario");
        JButton refrescar = Boton.secundario("", "refrescar");
        refrescar.addActionListener(e -> controlador.cargar());
        encabezado.agregarAccion(refrescar);
        if (Sesion.puede(Accion.GESTIONAR_INVENTARIO)) {
            JButton nueva = Boton.primario("Registrar compra", "mas");
            nueva.addActionListener(e -> controlador.registrar());
            encabezado.agregarAccion(nueva);
        }
        add(encabezado, BorderLayout.NORTH);
        add(tabla, BorderLayout.CENTER);
        controlador.cargar();
    }

    public void mostrar(List<Compra> compras) {
        tabla.setDatos(compras);
    }

    public Window ventana() {
        return SwingUtilities.getWindowAncestor(this);
    }

    public int filas() {
        return tabla.cantidadFilas();
    }
}
