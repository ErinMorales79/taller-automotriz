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

/** Formulario de servicios (mano de obra) con su precio base por hora. */
public final class EditorServicio implements EditorEntidad<Servicio> {

    private final Formulario formulario = new Formulario();
    private final JTextField nombre = formulario.agregarTexto("Nombre del servicio *");
    private final JTextField precio = formulario.agregarTexto("Precio base por hora *");

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(Servicio servicio) {
        nombre.setText(servicio.getNombre());
        precio.setText(servicio.getPrecioBase().toPlainString());
    }

    @Override
    public Servicio construir(Servicio existente) {
        BigDecimal precioEscrito = Formato.decimal(precio.getText(), "precio base");
        Servicio servicio = existente == null ? new Servicio(nombre.getText(), precioEscrito) : existente;
        servicio.setNombre(nombre.getText());
        servicio.setPrecioBase(precioEscrito);
        return servicio;
    }
}
