package com.taller.escritorio.vista.editor;

import com.taller.escritorio.Servicios;
import com.taller.escritorio.util.Formato;
import com.taller.escritorio.vista.componentes.EditorEntidad;
import com.taller.escritorio.vista.componentes.Formulario;
import com.taller.nucleo.entidad.*;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.servicio.Catalogos;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.math.BigDecimal;

/** Consumo real de un repuesto en una orden (aquí es donde se descuenta el stock, regla 3). */
public final class EditorConsumo implements EditorEntidad<EditorConsumo.DatosConsumo> {

    public record DatosConsumo(Repuesto repuesto, int cantidad) {
    }

    private final Formulario formulario = new Formulario();
    private final JComboBox<Repuesto> repuesto = formulario.agregar("Repuesto *", new JComboBox<>());
    private final JTextField cantidad = formulario.agregarTexto("Cantidad usada *");

    public EditorConsumo() {
        Servicios.CONSULTAS.repuestos("").forEach(repuesto::addItem);
        repuesto.setSelectedIndex(-1);
        repuesto.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> lista, Object valor, int indice,
                                                                  boolean elegido, boolean enfoque) {
                String texto = valor instanceof Repuesto r ? r.getNombre() + "  (stock: " + r.getStock() + ")" : "";
                return super.getListCellRendererComponent(lista, texto, indice, elegido, enfoque);
            }
        });
    }

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(DatosConsumo existente) {
        throw new UnsupportedOperationException("Un consumo registrado no se edita.");
    }

    @Override
    public DatosConsumo construir(DatosConsumo existente) {
        if (repuesto.getSelectedItem() == null) {
            throw new ReglaNegocioException("Elige el repuesto que se usó.");
        }
        return new DatosConsumo((Repuesto) repuesto.getSelectedItem(), Formato.entero(cantidad.getText(), "cantidad"));
    }
}
