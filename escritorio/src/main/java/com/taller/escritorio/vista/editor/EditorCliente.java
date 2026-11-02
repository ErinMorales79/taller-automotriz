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

/** Formulario de clientes (propietarios). */
public final class EditorCliente implements EditorEntidad<Cliente> {

    private final Formulario formulario = new Formulario();
    private final JTextField documento = formulario.agregarTexto("Documento *");
    private final JTextField nombre = formulario.agregarTexto("Nombre completo *");
    private final JTextField telefono = formulario.agregarTexto("Teléfono");
    private final JTextField correo = formulario.agregarTexto("Correo electrónico");
    private final JTextField direccion = formulario.agregarTexto("Dirección");

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(Cliente cliente) {
        documento.setText(cliente.getDocumento());
        nombre.setText(cliente.getNombre());
        telefono.setText(cliente.getTelefono());
        correo.setText(cliente.getCorreo());
        direccion.setText(cliente.getDireccion());
    }

    @Override
    public Cliente construir(Cliente existente) {
        Cliente cliente = existente == null ? new Cliente(documento.getText(), nombre.getText()) : existente;
        cliente.setDocumento(documento.getText());
        cliente.setNombre(nombre.getText());
        cliente.setTelefono(telefono.getText());
        cliente.setCorreo(correo.getText());
        cliente.setDireccion(direccion.getText());
        return cliente;
    }
}
