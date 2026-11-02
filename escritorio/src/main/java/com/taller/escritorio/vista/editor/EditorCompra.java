package com.taller.escritorio.vista.editor;

import com.taller.escritorio.Sesion;
import com.taller.escritorio.util.Formato;
import com.taller.escritorio.vista.componentes.Boton;
import com.taller.escritorio.vista.componentes.EditorEntidad;
import com.taller.escritorio.vista.componentes.Mensajes;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.escritorio.vista.componentes.Tema;
import com.taller.nucleo.entidad.Compra;
import com.taller.nucleo.entidad.Compra.DetalleCompra;
import com.taller.nucleo.entidad.Proveedor;
import com.taller.nucleo.entidad.Repuesto;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.servicio.Catalogos;

import javax.swing.BorderFactory;
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

/** Formulario maestro-detalle de una compra: proveedor y una lista de repuestos con cantidad y costo. */
public final class EditorCompra implements EditorEntidad<Compra> {

    private final JPanel panel = new JPanel(new BorderLayout(0, 12));
    private final JComboBox<Proveedor> proveedor = new JComboBox<>();
    private final JComboBox<Repuesto> repuesto = new JComboBox<>();
    private final JTextField cantidad = new JTextField(5);
    private final JTextField costo = new JTextField(8);
    private final JLabel total = new JLabel("Total: 0.00");
    private final List<DetalleCompra> detalles = new ArrayList<>();
    private final TablaDatos<DetalleCompra> tabla = new TablaDatos<>(List.of(
            new Columna<>("Repuesto", d -> d.repuesto().getNombre()),
            new Columna<>("Cantidad", DetalleCompra::cantidad),
            new Columna<>("Costo unitario", DetalleCompra::costoUnitario),
            new Columna<>("Subtotal", d -> d.costoUnitario().multiply(BigDecimal.valueOf(d.cantidad())))));

    public EditorCompra() {
        Catalogos.proveedores().listar().forEach(proveedor::addItem);
        Catalogos.repuestos().listar().forEach(repuesto::addItem);
        proveedor.setSelectedIndex(-1);
        repuesto.setSelectedIndex(-1);
        repuesto.addActionListener(e -> {
            if (repuesto.getSelectedItem() != null) {
                costo.setText(((Repuesto) repuesto.getSelectedItem()).getPrecioCosto().toPlainString());
            }
        });

        JPanel arriba = new JPanel(new BorderLayout(0, 4));
        arriba.setOpaque(false);
        arriba.add(etiqueta("Proveedor *"), BorderLayout.NORTH);
        arriba.add(proveedor, BorderLayout.CENTER);

        JButton agregar = Boton.secundario("Agregar", "mas");
        agregar.addActionListener(e -> {
            try {
                agregarLinea();
            } catch (ReglaNegocioException ex) {
                Mensajes.avisar(panel, ex.getMessage());
            }
        });
        JPanel linea = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linea.setOpaque(false);
        repuesto.setPreferredSize(new Dimension(230, 32));
        linea.add(repuesto);
        linea.add(etiqueta("Cant."));
        linea.add(cantidad);
        linea.add(etiqueta("Costo"));
        linea.add(costo);
        linea.add(agregar);

        JButton quitar = Boton.peligro("Quitar seleccionado", "borrar");
        quitar.addActionListener(e -> quitarLinea());
        total.setFont(total.getFont().deriveFont(Font.BOLD, 15f));
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(quitar, BorderLayout.WEST);
        pie.add(total, BorderLayout.EAST);

        tabla.setPreferredSize(new Dimension(640, 200));
        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);
        centro.add(linea, BorderLayout.NORTH);
        centro.add(tabla, BorderLayout.CENTER);
        centro.add(pie, BorderLayout.SOUTH);
        panel.setOpaque(false);
        panel.add(arriba, BorderLayout.NORTH);
        panel.add(centro, BorderLayout.CENTER);
    }

    private static JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(Tema.TEXTO_SUAVE);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return etiqueta;
    }

    private void agregarLinea() {
        Repuesto elegido = (Repuesto) repuesto.getSelectedItem();
        if (elegido == null) {
            throw new ReglaNegocioException("Elige el repuesto que se compró.");
        }
        int unidades = Formato.entero(cantidad.getText(), "cantidad");
        BigDecimal precio = Formato.decimal(costo.getText(), "costo");
        if (unidades <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero.");
        }
        detalles.add(new DetalleCompra(elegido, unidades, precio));
        cantidad.setText("");
        refrescar();
    }

    private void quitarLinea() {
        DetalleCompra elegido = tabla.seleccionado();
        if (elegido != null) {
            detalles.remove(elegido);
            refrescar();
        }
    }

    private void refrescar() {
        tabla.setDatos(detalles);
        BigDecimal suma = detalles.stream().map(d -> d.costoUnitario().multiply(BigDecimal.valueOf(d.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        total.setText("Total: " + Formato.dinero(suma));
    }

    @Override
    public JComponent componente() {
        return panel;
    }

    @Override
    public void cargar(Compra existente) {
        throw new UnsupportedOperationException("Las compras registradas no se editan.");
    }

    @Override
    public Compra construir(Compra existente) {
        Proveedor elegido = (Proveedor) proveedor.getSelectedItem();
        if (elegido == null) {
            throw new ReglaNegocioException("Elige el proveedor de la compra.");
        }
        if (detalles.isEmpty()) {
            throw new ReglaNegocioException("Agrega al menos un repuesto a la compra.");
        }
        Compra compra = new Compra(elegido, Sesion.usuario());
        detalles.forEach(d -> compra.agregarDetalle(d.repuesto(), d.cantidad(), d.costoUnitario()));
        return compra;
    }
}
