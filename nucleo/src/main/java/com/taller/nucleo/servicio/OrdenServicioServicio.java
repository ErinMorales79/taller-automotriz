package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.HistorialEstadoOrdenDao;
import com.taller.nucleo.dao.OrdenServicioDao;
import com.taller.nucleo.dao.jdbc.HistorialEstadoOrdenDaoJdbc;
import com.taller.nucleo.dao.jdbc.OrdenServicioDaoJdbc;
import com.taller.nucleo.dao.jdbc.PresupuestoDaoJdbc;
import com.taller.nucleo.entidad.EstadoPresupuesto;
import com.taller.nucleo.entidad.HistorialEstadoOrden;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Prioridad;
import com.taller.nucleo.entidad.TipoAtencion;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.entidad.Vehiculo;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.seguridad.Accion;
import com.taller.nucleo.seguridad.Permisos;
import com.taller.nucleo.util.Transaccion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Ciclo de vida de la orden. Aquí viven las reglas que dependen de otros datos:
 * 1 (orden duplicada), 2 (presupuesto aprobado), 4 (saldo o crédito) y 5 (historial).
 * Las transiciones permitidas las decide el patrón State de la propia orden.
 */
public final class OrdenServicioServicio {

    /** Verificación extra, con acceso a la base, que se ejecuta tras una transición válida. */
    @FunctionalInterface
    private interface Verificacion {
        void verificar(Connection conexion, OrdenServicio orden);
    }

    /**
     * Regla 1: no admite una segunda orden activa del mismo tipo para el vehículo salvo que un
     * administrador la autorice. Registra la creación en el historial (regla 5).
     */
    public OrdenServicio crear(Vehiculo vehiculo, TipoAtencion tipoAtencion, Prioridad prioridad,
                               Usuario registra, Usuario autorizaDuplicada) {
        Permisos.exigir(registra, Accion.CREAR_ORDEN);
        OrdenServicio orden = new OrdenServicio(vehiculo, tipoAtencion, prioridad, registra);
        int idVehiculo = Consultas.idGuardado(vehiculo.getId(), "el vehículo");
        int idTipo = Consultas.idGuardado(tipoAtencion.getId(), "el tipo de atención");
        return Transaccion.devolviendo(conexion -> {
            OrdenServicioDao ordenes = new OrdenServicioDaoJdbc(conexion);
            if (ordenes.existeActiva(idVehiculo, idTipo)) {
                if (autorizaDuplicada == null) {
                    throw new ReglaNegocioException("El vehículo " + vehiculo.getPlaca() + " ya tiene una orden activa de tipo "
                            + tipoAtencion.getNombre() + ". Se necesita la autorización de un administrador.");
                }
                orden.autorizarDuplicada(autorizaDuplicada);
            }
            ordenes.insertar(orden);
            AuditoriaServicio.registrar(conexion, registra, "CREAR_ORDEN", "orden_servicio", orden.getId());
            if (orden.getAutorizaDuplicada() != null) {
                AuditoriaServicio.registrar(conexion, autorizaDuplicada, "AUTORIZAR_DUPLICADA", "orden_servicio", orden.getId());
            }
            new HistorialEstadoOrdenDaoJdbc(conexion).insertar(
                    new HistorialEstadoOrden(orden, null, orden.getEstado().getNombre(), registra));
            return orden;
        });
    }

    public OrdenServicio iniciarDiagnostico(int idOrden, Usuario responsable) {
        return cambiar(idOrden, responsable, OrdenServicio::iniciarDiagnostico, null);
    }

    public OrdenServicio enviarAAprobacion(int idOrden, Usuario responsable) {
        return cambiar(idOrden, responsable, OrdenServicio::enviarAAprobacion, (conexion, orden) -> {
            boolean hayPendiente = new PresupuestoDaoJdbc(conexion).listarPorOrden(orden.getId()).stream()
                    .anyMatch(p -> p.getEstado() == EstadoPresupuesto.PENDIENTE);
            if (!hayPendiente) {
                throw new ReglaNegocioException(
                        "No se puede enviar a aprobación: la orden no tiene un presupuesto pendiente.");
            }
        });
    }

    public OrdenServicio volverADiagnostico(int idOrden, Usuario responsable) {
        return cambiar(idOrden, responsable, OrdenServicio::volverADiagnostico, null);
    }

    /** Regla 2: no se inicia la reparación sin un presupuesto aprobado. */
    public OrdenServicio iniciarReparacion(int idOrden, Usuario responsable) {
        return cambiar(idOrden, responsable, OrdenServicio::iniciarReparacion, (conexion, orden) -> {
            if (new PresupuestoDaoJdbc(conexion).buscarAprobadoDeOrden(orden.getId()).isEmpty()) {
                throw new ReglaNegocioException(
                        "No se puede iniciar la reparación: la orden no tiene un presupuesto aprobado.");
            }
        });
    }

    public OrdenServicio finalizar(int idOrden, Usuario responsable) {
        return cambiar(idOrden, responsable, OrdenServicio::finalizar, null);
    }

    /** Regla 4: además de estar finalizada, la orden necesita saldo cero o crédito autorizado. */
    public OrdenServicio entregar(int idOrden, Usuario responsable) {
        return cambiar(idOrden, responsable, OrdenServicio::entregar, (conexion, orden) -> {
            BigDecimal saldo = PagoServicio.saldo(conexion, orden.getId());
            if (saldo.signum() > 0 && !orden.tieneCreditoAutorizado()) {
                throw new ReglaNegocioException("No se puede entregar el vehículo: la orden tiene un saldo pendiente de "
                        + saldo + " y no tiene crédito autorizado.");
            }
        });
    }

    public OrdenServicio cancelar(int idOrden, Usuario responsable) {
        return cambiar(idOrden, responsable, OrdenServicio::cancelar, null);
    }

    /** Solo un administrador puede autorizar que se entregue con saldo pendiente. */
    public OrdenServicio autorizarCredito(int idOrden, Usuario administrador) {
        Permisos.exigir(administrador, Accion.AUTORIZAR_EXCEPCIONES);
        return Transaccion.devolviendo(conexion -> {
            OrdenServicio orden = Consultas.orden(conexion, idOrden);
            if (!orden.estaActiva()) {
                throw new ReglaNegocioException("No se puede autorizar crédito en una orden cerrada.");
            }
            orden.autorizarCredito(administrador);
            new OrdenServicioDaoJdbc(conexion).actualizar(orden);
            AuditoriaServicio.registrar(conexion, administrador, "AUTORIZAR_CREDITO", "orden_servicio", idOrden);
            return orden;
        });
    }

    /** Regla 5: cambios de estado de la orden, del más antiguo al más reciente. */
    public List<HistorialEstadoOrden> historial(int idOrden) {
        return Transaccion.devolviendo(conexion -> {
            HistorialEstadoOrdenDao dao = new HistorialEstadoOrdenDaoJdbc(conexion);
            return dao.listarPorOrden(idOrden);
        });
    }

    /**
     * Aplica una transición y la guarda con su registro en el historial (regla 5), todo en una
     * transacción: si algo falla, ni el estado ni el historial cambian.
     */
    private OrdenServicio cambiar(int idOrden, Usuario responsable, Consumer<OrdenServicio> transicion,
                                  Verificacion verificacion) {
        Permisos.exigir(responsable, Accion.GESTIONAR_ORDEN);
        return Transaccion.devolviendo(conexion -> {
            OrdenServicioDao ordenes = new OrdenServicioDaoJdbc(conexion);
            OrdenServicio orden = Consultas.orden(conexion, idOrden);
            String anterior = orden.getEstado().getNombre();
            transicion.accept(orden);
            if (verificacion != null) {
                verificacion.verificar(conexion, orden);
            }
            ordenes.actualizar(orden);
            new HistorialEstadoOrdenDaoJdbc(conexion).insertar(
                    new HistorialEstadoOrden(orden, anterior, orden.getEstado().getNombre(), responsable));
            AuditoriaServicio.registrar(conexion, responsable, "ORDEN_" + orden.getEstado().getNombre(),
                    "orden_servicio", orden.getId());
            return orden;
        });
    }
}
