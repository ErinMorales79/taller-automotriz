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

/** Datos para abrir una orden: vehículo, tipo de atención y prioridad. */
public final class EditorOrden implements EditorEntidad<EditorOrden.DatosOrden> {

    public record DatosOrden(Vehiculo vehiculo, TipoAtencion tipoAtencion, Prioridad prioridad) {
    }

    private final Formulario formulario = new Formulario();
    private final JComboBox<Vehiculo> vehiculo = formulario.agregar("Vehículo *", new JComboBox<>());
    private final JComboBox<TipoAtencion> tipo = formulario.agregar("Tipo de atención *", new JComboBox<>());
    private final JComboBox<Prioridad> prioridad = formulario.agregar("Prioridad *", new JComboBox<>(Prioridad.values()));

    public EditorOrden() {
        Catalogos.vehiculos().listar().forEach(vehiculo::addItem);
        Catalogos.tiposAtencion().listar().forEach(tipo::addItem);
        vehiculo.setSelectedIndex(-1);
        tipo.setSelectedIndex(-1);
        prioridad.setSelectedItem(Prioridad.MEDIA);
        vehiculo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> lista, Object valor, int indice,
                                                                  boolean elegido, boolean enfoque) {
                String texto = valor instanceof Vehiculo v ? v.getPlaca() + " - " + v.getModelo() + " (" + v.getPropietario().getNombre() + ")" : "";
                return super.getListCellRendererComponent(lista, texto, indice, elegido, enfoque);
            }
        });
    }

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(DatosOrden existente) {
        throw new UnsupportedOperationException("Una orden no se edita desde este formulario.");
    }

    @Override
    public DatosOrden construir(DatosOrden existente) {
        if (vehiculo.getSelectedItem() == null || tipo.getSelectedItem() == null) {
            throw new ReglaNegocioException("Elige el vehículo y el tipo de atención.");
        }
        return new DatosOrden((Vehiculo) vehiculo.getSelectedItem(), (TipoAtencion) tipo.getSelectedItem(),
                (Prioridad) prioridad.getSelectedItem());
    }
}
