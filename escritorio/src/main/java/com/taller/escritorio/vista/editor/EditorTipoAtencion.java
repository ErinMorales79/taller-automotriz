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

/** Formulario de tipos de atención (mantenimiento, reparación, etc.). */
public final class EditorTipoAtencion implements EditorEntidad<TipoAtencion> {

    private final Formulario formulario = new Formulario();
    private final JTextField nombre = formulario.agregarTexto("Nombre *");

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(TipoAtencion tipo) {
        nombre.setText(tipo.getNombre());
    }

    @Override
    public TipoAtencion construir(TipoAtencion existente) {
        TipoAtencion tipo = existente == null ? new TipoAtencion(nombre.getText()) : existente;
        tipo.setNombre(nombre.getText());
        return tipo;
    }
}
