package com.taller.escritorio.vista.orden;

import com.taller.escritorio.controlador.DetalleOrden;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.nucleo.entidad.HistorialEstadoOrden;

import java.awt.BorderLayout;
import java.util.List;

/** Regla 5: cada cambio de estado con su fecha y responsable. */
public final class PestanaHistorial extends Pestana {

    private final TablaDatos<HistorialEstadoOrden> tabla = new TablaDatos<>(List.of(
            new Columna<>("Fecha", HistorialEstadoOrden::getFecha),
            new Columna<>("Estado anterior", h -> h.getEstadoAnterior() == null ? "-" : h.getEstadoAnterior()),
            new Columna<>("Estado nuevo", HistorialEstadoOrden::getEstadoNuevo),
            new Columna<>("Responsable", h -> h.getResponsable().getNombreUsuario())));

    public PestanaHistorial() {
        add(tabla, BorderLayout.CENTER);
    }

    @Override
    public void mostrar(DetalleOrden detalle) {
        tabla.setDatos(detalle.historial());
    }
}
