package com.taller.escritorio.vista.orden;

import com.taller.escritorio.Sesion;
import com.taller.escritorio.controlador.ControladorOrden;
import com.taller.escritorio.controlador.DetalleOrden;
import com.taller.escritorio.vista.componentes.Boton;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.nucleo.entidad.EstadoPresupuesto;
import com.taller.nucleo.entidad.Presupuesto;
import com.taller.nucleo.seguridad.Accion;

import javax.swing.JButton;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Presupuestos de la orden con su detalle; aquí se crean, aprueban o rechazan. */
public final class PestanaPresupuestos extends Pestana {

    private record Linea(String tipo, String concepto, BigDecimal cantidad, BigDecimal precio, BigDecimal subtotal) {
    }

    private final TablaDatos<Presupuesto> presupuestos = new TablaDatos<>(List.of(
            new Columna<>("N.º", Presupuesto::getId), new Columna<>("Fecha", Presupuesto::getFecha),
            new Columna<>("Estado", Presupuesto::getEstado), new Columna<>("Total", Presupuesto::total),
            new Columna<>("Resolvió", p -> p.getAprobador() == null ? null : p.getAprobador().getNombreUsuario())));
    private final TablaDatos<Linea> lineas = new TablaDatos<>(List.of(
            new Columna<>("Tipo", Linea::tipo), new Columna<>("Concepto", Linea::concepto),
            new Columna<>("Horas / cantidad", Linea::cantidad), new Columna<>("Precio", Linea::precio),
            new Columna<>("Subtotal", Linea::subtotal)));
    private final JButton nuevo = Boton.primario("Nuevo presupuesto", "mas");
    private final JButton aprobar = Boton.secundario("Aprobar", "check");
    private final JButton rechazar = Boton.peligro("Rechazar", "cerrar");
    private String estadoOrden = "";

    public PestanaPresupuestos(ControladorOrden controlador) {
        nuevo.addActionListener(e -> controlador.nuevoPresupuesto());
        aprobar.addActionListener(e -> controlador.aprobar(presupuestos.seleccionado()));
        rechazar.addActionListener(e -> controlador.rechazar(presupuestos.seleccionado()));
        presupuestos.alElegir(this::alElegir);
        add(barra(nuevo, aprobar, rechazar), BorderLayout.NORTH);
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, presupuestos, lineas);
        division.setResizeWeight(0.45);
        division.setBorder(null);
        add(division, BorderLayout.CENTER);
    }

    @Override
    public void mostrar(DetalleOrden detalle) {
        estadoOrden = detalle.estado();
        presupuestos.setDatos(detalle.presupuestos());
        mostrarSi(nuevo, "EN_DIAGNOSTICO".equals(estadoOrden) && Sesion.puede(Accion.GESTIONAR_PRESUPUESTO));
        alElegir();
    }

    private void alElegir() {
        Presupuesto elegido = presupuestos.seleccionado();
        boolean resoluble = elegido != null && elegido.getEstado() == EstadoPresupuesto.PENDIENTE
                && "ESPERANDO_APROBACION".equals(estadoOrden) && Sesion.puede(Accion.GESTIONAR_PRESUPUESTO);
        mostrarSi(aprobar, resoluble);
        mostrarSi(rechazar, resoluble);
        List<Linea> detalle = new ArrayList<>();
        if (elegido != null) {
            elegido.getServicios().forEach(s -> detalle.add(new Linea("Mano de obra", s.servicio().getNombre(),
                    s.horas(), s.precioUnitario(), s.subtotal())));
            elegido.getRepuestos().forEach(r -> detalle.add(new Linea("Repuesto", r.repuesto().getNombre(),
                    BigDecimal.valueOf(r.cantidad()), r.precioUnitario(), r.subtotal())));
        }
        lineas.setDatos(detalle);
    }
}
