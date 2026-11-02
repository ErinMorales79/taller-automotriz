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

/** Formulario de vehículos: el propietario y el modelo se eligen de las listas existentes. */
public final class EditorVehiculo implements EditorEntidad<Vehiculo> {

    private final Formulario formulario = new Formulario();
    private final JTextField placa = formulario.agregarTexto("Placa *");
    private final JComboBox<Cliente> propietario = formulario.agregar("Propietario *", new JComboBox<>());
    private final JComboBox<Modelo> modelo = formulario.agregar("Marca y modelo *", new JComboBox<>());
    private final JTextField anio = formulario.agregarTexto("Año *");
    private final JTextField color = formulario.agregarTexto("Color");
    private final JTextField kilometraje = formulario.agregarTexto("Kilometraje");

    public EditorVehiculo() {
        Catalogos.clientes().listar().forEach(propietario::addItem);
        Catalogos.modelos().listar().forEach(modelo::addItem);
        propietario.setSelectedIndex(-1);
        modelo.setSelectedIndex(-1);
    }

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(Vehiculo vehiculo) {
        placa.setText(vehiculo.getPlaca());
        seleccionarPorId(propietario, vehiculo.getPropietario().getId());
        seleccionarPorId(modelo, vehiculo.getModelo().getId());
        anio.setText(String.valueOf(vehiculo.getAnio()));
        color.setText(vehiculo.getColor());
        kilometraje.setText(String.valueOf(vehiculo.getKilometraje()));
    }

    @Override
    public Vehiculo construir(Vehiculo existente) {
        Cliente dueno = (Cliente) propietario.getSelectedItem();
        Modelo modeloElegido = (Modelo) modelo.getSelectedItem();
        if (dueno == null || modeloElegido == null) {
            throw new com.taller.nucleo.excepcion.ReglaNegocioException("Elige el propietario y el modelo del vehículo.");
        }
        int anioEscrito = Formato.entero(anio.getText(), "año");
        Vehiculo vehiculo = existente == null ? new Vehiculo(placa.getText(), dueno, modeloElegido, anioEscrito) : existente;
        vehiculo.setPlaca(placa.getText());
        vehiculo.setPropietario(dueno);
        vehiculo.setModelo(modeloElegido);
        vehiculo.setAnio(anioEscrito);
        vehiculo.setColor(color.getText());
        vehiculo.setKilometraje(kilometraje.getText().isBlank() ? 0 : Formato.entero(kilometraje.getText(), "kilometraje"));
        return vehiculo;
    }

    private static <E> void seleccionarPorId(JComboBox<E> combo, Integer id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            Integer actual = item instanceof Cliente c ? c.getId() : ((Modelo) item).getId();
            if (actual.equals(id)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }
}
