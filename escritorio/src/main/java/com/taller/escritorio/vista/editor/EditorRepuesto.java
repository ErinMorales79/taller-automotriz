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

/**
 * Formulario de repuestos. El stock no se escribe aquí: solo cambia con compras y consumos
 * (regla 3), por eso se muestra de solo lectura.
 */
public final class EditorRepuesto implements EditorEntidad<Repuesto> {

    private final Formulario formulario = new Formulario();
    private final JTextField codigo = formulario.agregarTexto("Código *");
    private final JTextField nombre = formulario.agregarTexto("Nombre *");
    private final JTextField costo = formulario.agregarTexto("Precio de costo *");
    private final JTextField venta = formulario.agregarTexto("Precio de venta *");
    private final JTextField minimo = formulario.agregarTexto("Stock mínimo (aviso de stock bajo)");
    private final JTextField stock = formulario.agregarTexto("Stock actual (cambia con compras y consumos)");

    public EditorRepuesto() {
        stock.setEditable(false);
        stock.setText("0");
        minimo.setText("0");
    }

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(Repuesto repuesto) {
        codigo.setText(repuesto.getCodigo());
        nombre.setText(repuesto.getNombre());
        costo.setText(repuesto.getPrecioCosto().toPlainString());
        venta.setText(repuesto.getPrecioVenta().toPlainString());
        minimo.setText(String.valueOf(repuesto.getStockMinimo()));
        stock.setText(String.valueOf(repuesto.getStock()));
    }

    @Override
    public Repuesto construir(Repuesto existente) {
        BigDecimal precioCosto = Formato.decimal(costo.getText(), "precio de costo");
        BigDecimal precioVenta = Formato.decimal(venta.getText(), "precio de venta");
        Repuesto repuesto = existente == null
                ? new Repuesto(codigo.getText(), nombre.getText(), precioCosto, precioVenta) : existente;
        repuesto.setCodigo(codigo.getText());
        repuesto.setNombre(nombre.getText());
        repuesto.setPrecioCosto(precioCosto);
        repuesto.setPrecioVenta(precioVenta);
        repuesto.setStockMinimo(Formato.entero(minimo.getText(), "stock mínimo"));
        return repuesto;
    }
}
