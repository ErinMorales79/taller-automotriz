package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.AsignacionDao;
import com.taller.nucleo.dao.jdbc.AsignacionDaoJdbc;
import com.taller.nucleo.dao.jdbc.ObservacionOrdenDaoJdbc;
import com.taller.nucleo.dao.jdbc.TecnicoDaoJdbc;
import com.taller.nucleo.entidad.Asignacion;
import com.taller.nucleo.entidad.ObservacionOrden;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.AccesoDenegadoException;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.seguridad.Accion;
import com.taller.nucleo.seguridad.Permisos;
import com.taller.nucleo.util.Transaccion;

import java.util.List;

/** Asignación de técnicos y control de sus tareas (lo que luego usará la app móvil). */
public final class TareaServicio {

    public Asignacion asignar(int idOrden, int idTecnico, Usuario responsable) {
        Permisos.exigir(responsable, Accion.ASIGNAR_TECNICOS);
        return Transaccion.devolviendo(conexion -> {
            OrdenServicio orden = Consultas.orden(conexion, idOrden);
            if (!orden.estaActiva()) {
                throw new ReglaNegocioException("No se puede asignar un técnico a una orden cerrada.");
            }
            Tecnico tecnico = new TecnicoDaoJdbc(conexion).buscarPorId(idTecnico)
                    .orElseThrow(() -> new ReglaNegocioException("El técnico #" + idTecnico + " no existe."));
            if (!tecnico.isActivo()) {
                throw new ReglaNegocioException("El técnico " + tecnico.getNombre() + " no está activo.");
            }
            Asignacion asignacion = new AsignacionDaoJdbc(conexion).insertar(new Asignacion(orden, tecnico));
            AuditoriaServicio.registrar(conexion, responsable, "ASIGNAR_TECNICO", "asignacion", asignacion.getId());
            return asignacion;
        });
    }

    /** Un técnico solo puede actualizar sus propias tareas; el administrador, cualquiera. */
    public Asignacion actualizarAvance(int idAsignacion, int porcentaje, Usuario usuario) {
        Permisos.exigir(usuario, Accion.ACTUALIZAR_AVANCE);
        return Transaccion.devolviendo(conexion -> {
            AsignacionDao dao = new AsignacionDaoJdbc(conexion);
            Asignacion asignacion = dao.buscarPorId(idAsignacion)
                    .orElseThrow(() -> new ReglaNegocioException("La tarea #" + idAsignacion + " no existe."));
            if (usuario.getRol() == Rol.TECNICO && (usuario.getTecnico() == null
                    || !usuario.getTecnico().getId().equals(asignacion.getTecnico().getId()))) {
                throw new AccesoDenegadoException("Solo el técnico asignado puede actualizar el avance de esta tarea.");
            }
            asignacion.actualizarAvance(porcentaje);
            dao.actualizar(asignacion);
            AuditoriaServicio.registrar(conexion, usuario, "ACTUALIZAR_AVANCE", "asignacion", idAsignacion);
            return asignacion;
        });
    }

    /** Las tareas del técnico que inició sesión (lo que muestra la app móvil). */
    public List<Asignacion> asignacionesDelTecnico(Usuario usuario) {
        Tecnico tecnico = tecnicoDe(usuario);
        return Transaccion.devolviendo(conexion -> new AsignacionDaoJdbc(conexion).listarPorTecnico(tecnico.getId()));
    }

    /** La orden solo se entrega al técnico que tiene una tarea asignada en ella. */
    public OrdenServicio ordenAsignada(int idOrden, Usuario usuario) {
        Tecnico tecnico = tecnicoDe(usuario);
        return Transaccion.devolviendo(conexion -> ordenDelTecnico(conexion, idOrden, tecnico));
    }

    /** Observación de un técnico sobre una orden suya que sigue activa. */
    public ObservacionOrden agregarObservacion(int idOrden, String texto, Usuario usuario) {
        Tecnico tecnico = tecnicoDe(usuario);
        return Transaccion.devolviendo(conexion -> {
            OrdenServicio orden = ordenDelTecnico(conexion, idOrden, tecnico);
            if (!orden.estaActiva()) {
                throw new ReglaNegocioException("No se pueden agregar observaciones a una orden cerrada.");
            }
            ObservacionOrden observacion = new ObservacionOrdenDaoJdbc(conexion)
                    .insertar(new ObservacionOrden(orden, tecnico, texto));
            AuditoriaServicio.registrar(conexion, usuario, "AGREGAR_OBSERVACION", "observacion_orden", observacion.getId());
            return observacion;
        });
    }

    private static Tecnico tecnicoDe(Usuario usuario) {
        Permisos.exigir(usuario, Accion.ACTUALIZAR_AVANCE);
        if (usuario.getRol() != Rol.TECNICO || usuario.getTecnico() == null) {
            throw new AccesoDenegadoException("Esta operación es solo para técnicos del taller.");
        }
        return usuario.getTecnico();
    }

    private static OrdenServicio ordenDelTecnico(java.sql.Connection conexion, int idOrden, Tecnico tecnico) {
        OrdenServicio orden = Consultas.orden(conexion, idOrden);
        boolean asignado = new AsignacionDaoJdbc(conexion).listarPorOrden(idOrden).stream()
                .anyMatch(a -> a.getTecnico().getId().equals(tecnico.getId()));
        if (!asignado) {
            throw new AccesoDenegadoException("La orden #" + idOrden + " no está asignada a este técnico.");
        }
        return orden;
    }
}
