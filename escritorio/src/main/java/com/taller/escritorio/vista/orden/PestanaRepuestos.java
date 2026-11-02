package com.taller.escritorio.vista.orden;

import com.taller.escritorio.Sesion;
import com.taller.escritorio.controlador.ControladorOrden;
import com.taller.escritorio.controlador.DetalleOrden;
import com.taller.escritorio.vista.componentes.Boton;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.nucleo.entidad.ConsumoRepuesto;
import com.taller.nucleo.seguridad.Accion;

import javax.swing.JButton;
import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.List;

/** Repuestos realmente usados en la orden: cada registro descuenta stock (regla 3). */
public final class PestanaRepuestos extends Pestana {

    private final TablaDatos<ConsumoRepuesto> tabla = new TablaDatos<>(List.of(
            new Columna<>("Fecha", ConsumoRepuesto::getFecha), new Columna<>("Repuesto", c -> c.getRepuesto().getNombre()),
            new Columna<>("Cantidad", ConsumoRepuesto::getCantidad), new Columna<>("Precio unitario", ConsumoRepuesto::getPrecioUnitario),
            new Columna<>("Subtotal", c -> c.getPrecioUnitario().multiply(BigDecimal.valueOf(c.getCantidad()))),
            new Columna<>("Registró", c -> c.getUsuario().getNombreUsuario())));
    private final JButton registrar = Boton.primario("Registrar consumo", "mas");

    public PestanaRepuestos(ControladorOrden controlador) {
        registrar.addActionListener(e -> controlador.registrarConsumo());
        add(barra(registrar), BorderLayout.NORTH);
        add(tabla, BorderLayout.CENTER);
    }

    @Override
    public void mostrar(DetalleOrden detalle) {
        tabla.setDatos(detalle.consumos());
        mostrarSi(registrar, detalle.orden().estaActiva() && Sesion.puede(Accion.REGISTRAR_CONSUMO));
    }
}
