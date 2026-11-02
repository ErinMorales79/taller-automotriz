package com.taller.escritorio.controlador;

import com.taller.escritorio.Servicios;
import com.taller.escritorio.Sesion;
import com.taller.escritorio.util.Tarea;
import com.taller.escritorio.vista.PanelAuditoria;

/** Consulta la auditoría por tabla. */
public final class ControladorAuditoria {

    private final PanelAuditoria vista;

    public ControladorAuditoria(PanelAuditoria vista) {
        this.vista = vista;
    }

    public void cargar() {
        String tabla = vista.tablaElegida();
        Tarea.ejecutar(vista, () -> Servicios.AUDITORIA.consultarPorTabla(Sesion.usuario(), tabla), vista::mostrar);
    }
}
