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

/** Pago de una orden: monto y método. */
public final class EditorPago implements EditorEntidad<EditorPago.DatosPago> {

    public record DatosPago(BigDecimal monto, MetodoPago metodo) {
    }

    private final Formulario formulario = new Formulario();
    private final JTextField monto = formulario.agregarTexto("Monto *");
    private final JComboBox<MetodoPago> metodo = formulario.agregar("Método de pago *", new JComboBox<>(MetodoPago.values()));

    public EditorPago(BigDecimal saldoSugerido) {
        if (saldoSugerido != null && saldoSugerido.signum() > 0) {
            monto.setText(saldoSugerido.toPlainString());
        }
    }

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(DatosPago existente) {
        throw new UnsupportedOperationException("Un pago registrado no se edita.");
    }

    @Override
    public DatosPago construir(DatosPago existente) {
        return new DatosPago(Formato.decimal(monto.getText(), "monto"), (MetodoPago) metodo.getSelectedItem());
    }
}
