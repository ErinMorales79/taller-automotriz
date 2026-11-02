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

/** Elige el técnico al que se asigna la orden. */
public final class EditorAsignacion implements EditorEntidad<Tecnico> {

    private final Formulario formulario = new Formulario();
    private final JComboBox<Tecnico> tecnico = formulario.agregar("Técnico *", new JComboBox<>());

    public EditorAsignacion() {
        Servicios.CONSULTAS.tecnicosActivos().forEach(tecnico::addItem);
        tecnico.setSelectedIndex(-1);
    }

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(Tecnico existente) {
        throw new UnsupportedOperationException("Una asignación no se edita.");
    }

    @Override
    public Tecnico construir(Tecnico existente) {
        if (tecnico.getSelectedItem() == null) {
            throw new ReglaNegocioException("Elige el técnico que se asignará a la orden.");
        }
        return (Tecnico) tecnico.getSelectedItem();
    }
}
