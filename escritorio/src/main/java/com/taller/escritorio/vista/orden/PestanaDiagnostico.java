package com.taller.escritorio.vista.orden;

import com.taller.escritorio.Sesion;
import com.taller.escritorio.controlador.ControladorOrden;
import com.taller.escritorio.controlador.DetalleOrden;
import com.taller.escritorio.vista.componentes.Boton;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.nucleo.entidad.Diagnostico;
import com.taller.nucleo.seguridad.Accion;

import javax.swing.JButton;
import java.awt.BorderLayout;
import java.util.List;

/** Diagnósticos registrados; solo se agregan mientras la orden está en diagnóstico. */
public final class PestanaDiagnostico extends Pestana {

    private final TablaDatos<Diagnostico> tabla = new TablaDatos<>(List.of(
            new Columna<>("Fecha", Diagnostico::getFecha),
            new Columna<>("Técnico", d -> d.getTecnico().getNombre()),
            new Columna<>("Diagnóstico", Diagnostico::getDescripcion)));
    private final JButton registrar = Boton.primario("Registrar diagnóstico", "mas");

    public PestanaDiagnostico(ControladorOrden controlador) {
        registrar.addActionListener(e -> controlador.registrarDiagnostico());
        add(barra(registrar), BorderLayout.NORTH);
        add(tabla, BorderLayout.CENTER);
    }

    @Override
    public void mostrar(DetalleOrden detalle) {
        tabla.setDatos(detalle.diagnosticos());
        mostrarSi(registrar, "EN_DIAGNOSTICO".equals(detalle.estado()) && Sesion.puede(Accion.GESTIONAR_ORDEN));
    }
}
