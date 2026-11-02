package com.taller.escritorio.controlador;

import com.taller.nucleo.entidad.Asignacion;
import com.taller.nucleo.entidad.ConsumoRepuesto;
import com.taller.nucleo.entidad.Diagnostico;
import com.taller.nucleo.entidad.HistorialEstadoOrden;
import com.taller.nucleo.entidad.ObservacionOrden;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Pago;
import com.taller.nucleo.entidad.Presupuesto;
import com.taller.nucleo.entidad.EstadoPresupuesto;

import java.math.BigDecimal;
import java.util.List;

/** Todo lo que se muestra de una orden, leído de una vez para que las pestañas sean consistentes. */
public record DetalleOrden(OrdenServicio orden, BigDecimal totalAprobado, BigDecimal saldo,
                           List<Diagnostico> diagnosticos, List<Presupuesto> presupuestos,
                           List<Asignacion> asignaciones, List<ObservacionOrden> observaciones,
                           List<ConsumoRepuesto> consumos, List<Pago> pagos,
                           List<HistorialEstadoOrden> historial) {

    public String estado() {
        return orden.getEstado().getNombre();
    }

    public BigDecimal totalPagado() {
        return pagos.stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean tienePresupuestoAprobado() {
        return presupuestos.stream().anyMatch(p -> p.getEstado() == EstadoPresupuesto.APROBADO);
    }
}
