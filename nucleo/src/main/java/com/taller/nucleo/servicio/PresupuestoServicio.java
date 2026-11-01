package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.PresupuestoDao;
import com.taller.nucleo.dao.jdbc.PresupuestoDaoJdbc;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Presupuesto;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.estado.EnDiagnostico;
import com.taller.nucleo.estado.EsperandoAprobacion;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.seguridad.Accion;
import com.taller.nucleo.seguridad.Permisos;
import com.taller.nucleo.util.Transaccion;

/**
 * Presupuestos sujetos a aprobación. Un presupuesto rechazado se conserva con ese estado:
 * la orden queda esperando y recepción decide si vuelve a diagnóstico o se cancela.
 */
public final class PresupuestoServicio {

    /** Guarda el presupuesto con todos sus detalles en una sola transacción. */
    public Presupuesto crear(Presupuesto presupuesto, Usuario responsable) {
        Permisos.exigir(responsable, Accion.GESTIONAR_PRESUPUESTO);
        if (presupuesto.estaVacio()) {
            throw new ReglaNegocioException("El presupuesto debe tener al menos un servicio o un repuesto.");
        }
        int idOrden = Consultas.idGuardado(presupuesto.getOrden().getId(), "la orden del presupuesto");
        return Transaccion.devolviendo(conexion -> {
            OrdenServicio orden = Consultas.orden(conexion, idOrden);
            if (!(orden.getEstado() instanceof EnDiagnostico)) {
                throw new ReglaNegocioException(
                        "Solo se puede crear un presupuesto mientras la orden está en diagnóstico.");
            }
            new PresupuestoDaoJdbc(conexion).insertar(presupuesto);
            AuditoriaServicio.registrar(conexion, responsable, "CREAR_PRESUPUESTO", "presupuesto", presupuesto.getId());
            return presupuesto;
        });
    }

    public Presupuesto aprobar(int idPresupuesto, Usuario responsable) {
        Permisos.exigir(responsable, Accion.GESTIONAR_PRESUPUESTO);
        return Transaccion.devolviendo(conexion -> {
            PresupuestoDao dao = new PresupuestoDaoJdbc(conexion);
            Presupuesto presupuesto = Consultas.presupuesto(conexion, idPresupuesto);
            exigirOrdenEsperando(presupuesto);
            if (dao.buscarAprobadoDeOrden(presupuesto.getOrden().getId()).isPresent()) {
                throw new ReglaNegocioException("La orden ya tiene un presupuesto aprobado.");
            }
            presupuesto.aprobar(responsable);
            dao.actualizarEstado(presupuesto);
            AuditoriaServicio.registrar(conexion, responsable, "APROBAR_PRESUPUESTO", "presupuesto", idPresupuesto);
            return presupuesto;
        });
    }

    public Presupuesto rechazar(int idPresupuesto, Usuario responsable) {
        Permisos.exigir(responsable, Accion.GESTIONAR_PRESUPUESTO);
        return Transaccion.devolviendo(conexion -> {
            Presupuesto presupuesto = Consultas.presupuesto(conexion, idPresupuesto);
            exigirOrdenEsperando(presupuesto);
            presupuesto.rechazar(responsable);
            new PresupuestoDaoJdbc(conexion).actualizarEstado(presupuesto);
            AuditoriaServicio.registrar(conexion, responsable, "RECHAZAR_PRESUPUESTO", "presupuesto", idPresupuesto);
            return presupuesto;
        });
    }

    private static void exigirOrdenEsperando(Presupuesto presupuesto) {
        if (!(presupuesto.getOrden().getEstado() instanceof EsperandoAprobacion)) {
            throw new ReglaNegocioException(
                    "El presupuesto solo se resuelve mientras la orden espera su aprobación.");
        }
    }
}
