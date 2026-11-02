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

/** Formulario de proveedores de repuestos. */
public final class EditorProveedor implements EditorEntidad<Proveedor> {

    private final Formulario formulario = new Formulario();
    private final JTextField nombre = formulario.agregarTexto("Nombre *");
    private final JTextField telefono = formulario.agregarTexto("Teléfono");

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(Proveedor proveedor) {
        nombre.setText(proveedor.getNombre());
        telefono.setText(proveedor.getTelefono());
    }

    @Override
    public Proveedor construir(Proveedor existente) {
        Proveedor proveedor = existente == null ? new Proveedor(nombre.getText()) : existente;
        proveedor.setNombre(nombre.getText());
        proveedor.setTelefono(telefono.getText());
        return proveedor;
    }
}
