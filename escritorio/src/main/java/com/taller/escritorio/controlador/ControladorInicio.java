package com.taller.escritorio.controlador;

import com.taller.escritorio.Servicios;
import com.taller.escritorio.util.Tarea;
import com.taller.escritorio.vista.PanelInicio;
import com.taller.nucleo.entidad.OrdenServicio;

import java.util.List;

/** Calcula los números del panel de inicio. */
public final class ControladorInicio {

    public record Resumen(long activas, long esperandoAprobacion, long enReparacion, int stockBajo,
                          List<OrdenServicio> recientes) {
    }

    private final PanelInicio vista;

    public ControladorInicio(PanelInicio vista) {
        this.vista = vista;
    }

    public void cargar() {
        Tarea.ejecutar(vista, () -> {
            List<OrdenServicio> ordenes = Servicios.CONSULTAS.ordenes(null, null);
            return new Resumen(
                    ordenes.stream().filter(OrdenServicio::estaActiva).count(),
                    contar(ordenes, "ESPERANDO_APROBACION"),
                    contar(ordenes, "EN_REPARACION"),
                    Servicios.CONSULTAS.repuestosConStockBajo().size(),
                    ordenes.stream().limit(8).toList());
        }, vista::mostrar);
    }

    private static long contar(List<OrdenServicio> ordenes, String estado) {
        return ordenes.stream().filter(o -> o.getEstado().getNombre().equals(estado)).count();
    }
}
