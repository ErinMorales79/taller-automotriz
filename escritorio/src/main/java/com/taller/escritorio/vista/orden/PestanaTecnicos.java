package com.taller.escritorio.vista.orden;

import com.taller.escritorio.Sesion;
import com.taller.escritorio.controlador.ControladorOrden;
import com.taller.escritorio.controlador.DetalleOrden;
import com.taller.escritorio.vista.componentes.Boton;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.nucleo.entidad.Asignacion;
import com.taller.nucleo.entidad.ObservacionOrden;
import com.taller.nucleo.seguridad.Accion;

import javax.swing.JButton;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.util.List;

/** Técnicos asignados con su avance y las observaciones que dejaron desde la app móvil. */
public final class PestanaTecnicos extends Pestana {

    private final TablaDatos<Asignacion> asignaciones = new TablaDatos<>(List.of(
            new Columna<>("Técnico", a -> a.getTecnico().getNombre()), new Columna<>("Asignado", Asignacion::getFechaAsignacion),
            new Columna<>("Tarea", Asignacion::getEstadoTarea), new Columna<>("Avance %", Asignacion::getPorcentajeAvance)));
    private final TablaDatos<ObservacionOrden> observaciones = new TablaDatos<>(List.of(
            new Columna<>("Fecha", ObservacionOrden::getFecha), new Columna<>("Técnico", o -> o.getTecnico().getNombre()),
            new Columna<>("Observación", ObservacionOrden::getTexto)));
    private final JButton asignar = Boton.primario("Asignar técnico", "mas");

    public PestanaTecnicos(ControladorOrden controlador) {
        asignar.addActionListener(e -> controlador.asignarTecnico());
        add(barra(asignar), BorderLayout.NORTH);
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, asignaciones, observaciones);
        division.setResizeWeight(0.5);
        division.setBorder(null);
        add(division, BorderLayout.CENTER);
    }

    @Override
    public void mostrar(DetalleOrden detalle) {
        asignaciones.setDatos(detalle.asignaciones());
        observaciones.setDatos(detalle.observaciones());
        mostrarSi(asignar, detalle.orden().estaActiva() && Sesion.puede(Accion.ASIGNAR_TECNICOS));
    }
}
