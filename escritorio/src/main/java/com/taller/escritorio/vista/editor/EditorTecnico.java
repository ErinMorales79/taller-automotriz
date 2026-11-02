package com.taller.escritorio.vista.editor;

import com.taller.escritorio.util.Formato;
import com.taller.escritorio.vista.componentes.EditorEntidad;
import com.taller.escritorio.vista.componentes.Formulario;
import com.taller.nucleo.entidad.*;
import com.taller.nucleo.servicio.Catalogos;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import java.math.BigDecimal;

/** Formulario de técnicos del taller. */
public final class EditorTecnico implements EditorEntidad<Tecnico> {

    private final Formulario formulario = new Formulario();
    private final JTextField nombre = formulario.agregarTexto("Nombre completo *");
    private final JTextField especialidad = formulario.agregarTexto("Especialidad");
    private final javax.swing.JCheckBox activo = formulario.agregar("Estado", new javax.swing.JCheckBox("Activo (puede recibir órdenes)", true));

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(Tecnico tecnico) {
        nombre.setText(tecnico.getNombre());
        especialidad.setText(tecnico.getEspecialidad());
        activo.setSelected(tecnico.isActivo());
    }

    @Override
    public Tecnico construir(Tecnico existente) {
        Tecnico tecnico = existente == null ? new Tecnico(nombre.getText()) : existente;
        tecnico.setNombre(nombre.getText());
        tecnico.setEspecialidad(especialidad.getText());
        tecnico.setActivo(activo.isSelected());
        return tecnico;
    }
}
