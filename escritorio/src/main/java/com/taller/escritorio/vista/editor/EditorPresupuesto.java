package com.taller.escritorio.vista.editor;

import com.taller.escritorio.util.Formato;
import com.taller.escritorio.vista.componentes.Boton;
import com.taller.escritorio.vista.componentes.EditorEntidad;
import com.taller.escritorio.vista.componentes.Mensajes;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.escritorio.vista.componentes.Tema;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Presupuesto;
import com.taller.nucleo.entidad.Repuesto;
import com.taller.nucleo.entidad.Servicio;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.servicio.Catalogos;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Formulario maestro-detalle del presupuesto: servicios (horas) y repuestos (cantidad) con su total. */
public final class EditorPresupuesto implements EditorEntidad<Presupuesto> {

    private record Linea(Servicio servicio, BigDecimal horas, Repuesto repuesto, int cantidad) {
        String tipo() {
            return servicio != null ? "Mano de obra" : "Repuesto";
        }

        String concepto() {
            return servicio != null ? servicio.getNombre() : repuesto.getNombre();
        }

        BigDecimal cantidadMostrada() {
            return servicio != null ? horas : BigDecimal.valueOf(cantidad);
        }

        BigDecimal precio() {
            return servicio != null ? servicio.getPrecioBase() : repuesto.getPrecioVenta();
        }

        BigDecimal subtotal() {
            return precio().multiply(cantidadMostrada());
        }
    }

    private final OrdenServicio orden;
    private final JPanel panel = new JPanel(new BorderLayout(0, 12));
    private final JComboBox<Servicio> servicio = new JComboBox<>();
    private final JTextField horas = new JTextField(5);
    private final JComboBox<Repuesto> repuesto = new JComboBox<>();
    private final JTextField cantidad = new JTextField(5);
    private final JLabel total = new JLabel("Total: 0.00");
    private final List<Linea> lineas = new ArrayList<>();
    private final TablaDatos<Linea> tabla = new TablaDatos<>(List.of(
            new Columna<>("Tipo", Linea::tipo), new Columna<>("Concepto", Linea::concepto),
            new Columna<>("Horas / cantidad", Linea::cantidadMostrada), new Columna<>("Precio", Linea::precio),
            new Columna<>("Subtotal", Linea::subtotal)));

    public EditorPresupuesto(OrdenServicio orden) {
        this.orden = orden;
        Catalogos.servicios().listar().forEach(servicio::addItem);
        Catalogos.repuestos().listar().forEach(repuesto::addItem);
        servicio.setSelectedIndex(-1);
        repuesto.setSelectedIndex(-1);

        JButton agregarServicio = Boton.secundario("Agregar servicio", "mas");
        agregarServicio.addActionListener(e -> proteger(this::agregarServicio));
        JButton agregarRepuesto = Boton.secundario("Agregar repuesto", "mas");
        agregarRepuesto.addActionListener(e -> proteger(this::agregarRepuesto));
        servicio.setPreferredSize(new Dimension(220, 32));
        repuesto.setPreferredSize(new Dimension(220, 32));

        JPanel filaServicio = fila(servicio, "Horas", horas, agregarServicio);
        JPanel filaRepuesto = fila(repuesto, "Cant.", cantidad, agregarRepuesto);
        JPanel entradas = new JPanel(new BorderLayout(0, 6));
        entradas.setOpaque(false);
        entradas.add(filaServicio, BorderLayout.NORTH);
        entradas.add(filaRepuesto, BorderLayout.SOUTH);

        JButton quitar = Boton.peligro("Quitar seleccionado", "borrar");
        quitar.addActionListener(e -> {
            Linea elegida = tabla.seleccionado();
            if (elegida != null) {
                lineas.remove(elegida);
                refrescar();
            }
        });
        total.setFont(total.getFont().deriveFont(Font.BOLD, 15f));
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(quitar, BorderLayout.WEST);
        pie.add(total, BorderLayout.EAST);

        tabla.setPreferredSize(new Dimension(720, 220));
        panel.setOpaque(false);
        panel.add(entradas, BorderLayout.NORTH);
        panel.add(tabla, BorderLayout.CENTER);
        panel.add(pie, BorderLayout.SOUTH);
    }

    private static JPanel fila(JComponent combo, String etiqueta, JTextField campo, JButton boton) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fila.setOpaque(false);
        JLabel texto = new JLabel(etiqueta);
        texto.setForeground(Tema.TEXTO_SUAVE);
        fila.add(combo);
        fila.add(texto);
        fila.add(campo);
        fila.add(boton);
        return fila;
    }

    private void proteger(Runnable accion) {
        try {
            accion.run();
        } catch (ReglaNegocioException e) {
            Mensajes.avisar(panel, e.getMessage());
        }
    }

    private void agregarServicio() {
        Servicio elegido = (Servicio) servicio.getSelectedItem();
        if (elegido == null) {
            throw new ReglaNegocioException("Elige el servicio de mano de obra.");
        }
        BigDecimal horasEscritas = Formato.decimal(horas.getText(), "horas");
        if (horasEscritas.signum() <= 0) {
            throw new ReglaNegocioException("Las horas deben ser mayores que cero.");
        }
        lineas.add(new Linea(elegido, horasEscritas, null, 0));
        horas.setText("");
        refrescar();
    }

    private void agregarRepuesto() {
        Repuesto elegido = (Repuesto) repuesto.getSelectedItem();
        if (elegido == null) {
            throw new ReglaNegocioException("Elige el repuesto.");
        }
        int unidades = Formato.entero(cantidad.getText(), "cantidad");
        if (unidades <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero.");
        }
        lineas.add(new Linea(null, null, elegido, unidades));
        cantidad.setText("");
        refrescar();
    }

    private void refrescar() {
        tabla.setDatos(lineas);
        total.setText("Total: " + Formato.dinero(lineas.stream().map(Linea::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    @Override
    public JComponent componente() {
        return panel;
    }

    @Override
    public void cargar(Presupuesto existente) {
        throw new UnsupportedOperationException("Un presupuesto registrado no se edita: se crea uno nuevo.");
    }

    @Override
    public Presupuesto construir(Presupuesto existente) {
        if (lineas.isEmpty()) {
            throw new ReglaNegocioException("El presupuesto debe tener al menos un servicio o un repuesto.");
        }
        Presupuesto presupuesto = new Presupuesto(orden);
        for (Linea linea : lineas) {
            if (linea.servicio() != null) {
                presupuesto.agregarServicio(linea.servicio(), linea.horas());
            } else {
                presupuesto.agregarRepuesto(linea.repuesto(), linea.cantidad());
            }
        }
        return presupuesto;
    }
}
