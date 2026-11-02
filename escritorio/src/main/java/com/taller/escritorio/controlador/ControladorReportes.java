package com.taller.escritorio.controlador;

import com.taller.escritorio.Servicios;
import com.taller.escritorio.Sesion;
import com.taller.escritorio.util.Formato;
import com.taller.escritorio.util.Tarea;
import com.taller.escritorio.vista.PanelReportes;
import com.taller.escritorio.vista.componentes.Mensajes;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.entidad.Vehiculo;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.reporte.Reporte;
import com.taller.nucleo.servicio.Catalogos;

import java.nio.file.Path;
import java.time.LocalDate;

/** Genera los reportes con ReporteServicio y los exporta a PDF o Excel. */
public final class ControladorReportes {

    private final PanelReportes vista;
    private Reporte ultimo;

    public ControladorReportes(PanelReportes vista) {
        this.vista = vista;
    }

    public void cargarVehiculos() {
        Tarea.ejecutar(vista, () -> Catalogos.vehiculos().listar(), vista::mostrarVehiculos);
    }

    public void generar(TipoReporte tipo, String desdeEscrito, String hastaEscrito, String limiteEscrito, Vehiculo vehiculo) {
        Usuario usuario = Sesion.usuario();
        Tarea.ejecutar(vista, () -> {
            LocalDate desde = Formato.fechaOpcional(desdeEscrito, "desde");
            LocalDate hasta = Formato.fechaOpcional(hastaEscrito, "hasta");
            return switch (tipo) {
                case ORDENES_POR_ESTADO -> Servicios.REPORTES.ordenesPorEstado(usuario, desde, hasta);
                case PRODUCTIVIDAD -> Servicios.REPORTES.productividadPorTecnico(usuario, desde, hasta);
                case REPUESTOS -> Servicios.REPORTES.repuestosMasConsumidos(usuario, desde, hasta,
                        limiteEscrito.isBlank() ? 10 : Formato.entero(limiteEscrito, "límite"));
                case INGRESOS -> Servicios.REPORTES.ingresosPorServicio(usuario, desde, hasta);
                case HISTORIAL_VEHICULO -> {
                    if (vehiculo == null) {
                        throw new ReglaNegocioException("Elige el vehículo del que quieres el historial.");
                    }
                    yield Servicios.REPORTES.historialDeVehiculo(usuario, vehiculo.getId());
                }
            };
        }, reporte -> {
            ultimo = reporte;
            vista.mostrar(reporte);
        });
    }

    public void exportar(boolean aPdf) {
        if (ultimo == null) {
            Mensajes.avisar(vista, "Primero genera un reporte para poder exportarlo.");
            return;
        }
        Path destino = vista.elegirDestino(aPdf ? "pdf" : "xlsx", ultimo.titulo());
        if (destino == null) {
            return;
        }
        Reporte reporte = ultimo;
        Tarea.ejecutar(vista, () -> {
            if (aPdf) {
                Servicios.REPORTES.exportarPdf(Sesion.usuario(), reporte, destino);
            } else {
                Servicios.REPORTES.exportarExcel(Sesion.usuario(), reporte, destino);
            }
        }, () -> Mensajes.informar(vista, "Reporte exportado en:\n" + destino));
    }
}
