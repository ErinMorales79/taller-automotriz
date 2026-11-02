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

/** Formulario de modelos: cada modelo pertenece a una marca. */
public final class EditorModelo implements EditorEntidad<Modelo> {

    private final Formulario formulario = new Formulario();
    private final JComboBox<Marca> marca = formulario.agregar("Marca *", new JComboBox<>());
    private final JTextField nombre = formulario.agregarTexto("Nombre del modelo *");

    public EditorModelo() {
        Catalogos.marcas().listar().forEach(marca::addItem);
        marca.setSelectedIndex(-1);
    }

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(Modelo modelo) {
        for (int i = 0; i < marca.getItemCount(); i++) {
            if (marca.getItemAt(i).getId().equals(modelo.getMarca().getId())) {
                marca.setSelectedIndex(i);
            }
        }
        nombre.setText(modelo.getNombre());
    }

    @Override
    public Modelo construir(Modelo existente) {
        Marca elegida = (Marca) marca.getSelectedItem();
        if (elegida == null) {
            throw new com.taller.nucleo.excepcion.ReglaNegocioException("Elige la marca del modelo.");
        }
        Modelo modelo = existente == null ? new Modelo(elegida, nombre.getText()) : existente;
        modelo.setMarca(elegida);
        modelo.setNombre(nombre.getText());
        return modelo;
    }
}
