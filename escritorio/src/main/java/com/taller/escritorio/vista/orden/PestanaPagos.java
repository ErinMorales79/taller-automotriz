package com.taller.escritorio.vista.orden;

import com.taller.escritorio.Sesion;
import com.taller.escritorio.controlador.ControladorOrden;
import com.taller.escritorio.controlador.DetalleOrden;
import com.taller.escritorio.util.Formato;
import com.taller.escritorio.vista.componentes.Boton;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.escritorio.vista.componentes.Tema;
import com.taller.nucleo.entidad.Pago;
import com.taller.nucleo.seguridad.Accion;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

/** Pagos recibidos y saldo: el saldo es el total aprobado menos lo pagado. */
public final class PestanaPagos extends Pestana {

    private final TablaDatos<Pago> tabla = new TablaDatos<>(List.of(
            new Columna<>("Fecha", Pago::getFecha), new Columna<>("Monto", Pago::getMonto),
            new Columna<>("Método", Pago::getMetodo), new Columna<>("Registró", p -> p.getUsuario().getNombreUsuario())));
    private final JButton registrar = Boton.primario("Registrar pago", "mas");
    private final JLabel saldo = new JLabel();

    public PestanaPagos(ControladorOrden controlador) {
        registrar.addActionListener(e -> controlador.registrarPago());
        saldo.setFont(saldo.getFont().deriveFont(Font.BOLD, 15f));
        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        arriba.add(barra(registrar), BorderLayout.WEST);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(saldo);
        arriba.add(derecha, BorderLayout.EAST);
        add(arriba, BorderLayout.NORTH);
        add(tabla, BorderLayout.CENTER);
    }

    @Override
    public void mostrar(DetalleOrden detalle) {
        tabla.setDatos(detalle.pagos());
        saldo.setText("Saldo pendiente: " + Formato.dinero(detalle.saldo()));
        saldo.setForeground(detalle.saldo().signum() > 0 ? Tema.ADVERTENCIA : Tema.EXITO);
        mostrarSi(registrar, !"CANCELADA".equals(detalle.estado()) && detalle.saldo().signum() > 0
                && Sesion.puede(Accion.REGISTRAR_PAGO));
    }
}
