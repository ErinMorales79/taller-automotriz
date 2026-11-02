package com.taller.escritorio.vista.orden;

import com.taller.escritorio.Sesion;
import com.taller.escritorio.controlador.ControladorOrden;
import com.taller.escritorio.controlador.DetalleOrden;
import com.taller.escritorio.controlador.Transicion;
import com.taller.escritorio.util.Formato;
import com.taller.escritorio.vista.componentes.Boton;
import com.taller.escritorio.vista.componentes.Tarjeta;
import com.taller.escritorio.vista.componentes.Tema;
import com.taller.nucleo.entidad.OrdenServicio;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.FlowLayout;

/** Datos generales, dinero de la orden y los cambios de estado que se pueden pedir ahora. */
public final class PestanaResumen extends Pestana {

    private final ControladorOrden controlador;
    private final Tarjeta datos = new Tarjeta(new GridBagLayout());
    private final JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

    public PestanaResumen(ControladorOrden controlador) {
        this.controlador = controlador;
        acciones.setOpaque(false);
        add(acciones, BorderLayout.NORTH);
        add(datos, BorderLayout.CENTER);
    }

    @Override
    public void mostrar(DetalleOrden detalle) {
        OrdenServicio orden = detalle.orden();
        acciones.removeAll();
        for (Transicion transicion : Transicion.desde(detalle.estado())) {
            if (Sesion.puede(transicion.permiso())) {
                JButton boton = transicion.esPeligrosa() ? Boton.peligro(transicion.texto(), transicion.icono())
                        : Boton.primario(transicion.texto(), transicion.icono());
                boton.addActionListener(e -> controlador.cambiarEstado(transicion));
                acciones.add(boton);
            }
        }
        acciones.revalidate();
        acciones.repaint();

        datos.removeAll();
        int fila = 0;
        fila = linea(fila, "Orden", "#" + orden.getId());
        fila = linea(fila, "Estado", Formato.legible(detalle.estado()));
        fila = linea(fila, "Vehículo", orden.getVehiculo().getPlaca() + " - " + orden.getVehiculo().getModelo()
                + " (" + orden.getVehiculo().getAnio() + ")");
        fila = linea(fila, "Propietario", orden.getVehiculo().getPropietario().getNombre()
                + valorOpcional(orden.getVehiculo().getPropietario().getTelefono(), "  -  Tel. "));
        fila = linea(fila, "Tipo de atención", orden.getTipoAtencion().getNombre());
        fila = linea(fila, "Prioridad", Formato.legible(orden.getPrioridad()));
        fila = linea(fila, "Ingreso", Formato.fechaHora(orden.getFechaIngreso()));
        fila = linea(fila, "Entrega", Formato.fechaHora(orden.getFechaEntrega()));
        fila = linea(fila, "Registró", orden.getUsuarioRegistro().getNombreUsuario());
        if (orden.getAutorizaDuplicada() != null) {
            fila = linea(fila, "Duplicada autorizada por", orden.getAutorizaDuplicada().getNombreUsuario());
        }
        if (orden.getAutorizaCredito() != null) {
            fila = linea(fila, "Crédito autorizado por", orden.getAutorizaCredito().getNombreUsuario());
        }
        fila = linea(fila, "Total aprobado", Formato.dinero(detalle.totalAprobado()));
        fila = linea(fila, "Pagado", Formato.dinero(detalle.totalPagado()));
        linea(fila, "Saldo pendiente", Formato.dinero(detalle.saldo()));
        GridBagConstraints relleno = new GridBagConstraints();
        relleno.gridy = fila + 1;
        relleno.weighty = 1;
        datos.add(javax.swing.Box.createVerticalGlue(), relleno);
        datos.revalidate();
        datos.repaint();
    }

    private static String valorOpcional(String valor, String prefijo) {
        return valor == null || valor.isBlank() ? "" : prefijo + valor;
    }

    private int linea(int fila, String titulo, String valor) {
        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setForeground(Tema.TEXTO_SUAVE);
        JLabel contenido = new JLabel(valor);
        contenido.setFont(contenido.getFont().deriveFont(Font.BOLD));
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 0, 5, 40);
        c.gridx = 0;
        c.weightx = 0;
        datos.add(etiqueta, c);
        c = new GridBagConstraints();
        c.gridy = fila;
        c.gridx = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        datos.add(contenido, c);
        return fila + 1;
    }
}
