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

/** Diagnóstico de una orden: qué técnico lo hizo y qué encontró. */
public final class EditorDiagnostico implements EditorEntidad<EditorDiagnostico.DatosDiagnostico> {

    public record DatosDiagnostico(Tecnico tecnico, String descripcion) {
    }

    private final Formulario formulario = new Formulario();
    private final JComboBox<Tecnico> tecnico = formulario.agregar("Técnico que diagnostica *", new JComboBox<>());
    private final JTextArea descripcion = formulario.agregar("Descripción del diagnóstico *", new JTextArea(6, 34));

    public EditorDiagnostico() {
        Servicios.CONSULTAS.tecnicosActivos().forEach(tecnico::addItem);
        tecnico.setSelectedIndex(-1);
        descripcion.setLineWrap(true);
        descripcion.setWrapStyleWord(true);
    }

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(DatosDiagnostico existente) {
        throw new UnsupportedOperationException("Los diagnósticos registrados no se editan.");
    }

    @Override
    public DatosDiagnostico construir(DatosDiagnostico existente) {
        if (tecnico.getSelectedItem() == null) {
            throw new ReglaNegocioException("Elige el técnico que hizo el diagnóstico.");
        }
        return new DatosDiagnostico((Tecnico) tecnico.getSelectedItem(), descripcion.getText());
    }
}
