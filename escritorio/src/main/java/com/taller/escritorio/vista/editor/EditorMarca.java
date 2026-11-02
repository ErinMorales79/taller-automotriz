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

/** Formulario de marcas de vehículos. */
public final class EditorMarca implements EditorEntidad<Marca> {

    private final Formulario formulario = new Formulario();
    private final JTextField nombre = formulario.agregarTexto("Nombre *");

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(Marca marca) {
        nombre.setText(marca.getNombre());
    }

    @Override
    public Marca construir(Marca existente) {
        Marca marca = existente == null ? new Marca(nombre.getText()) : existente;
        marca.setNombre(nombre.getText());
        return marca;
    }
}
