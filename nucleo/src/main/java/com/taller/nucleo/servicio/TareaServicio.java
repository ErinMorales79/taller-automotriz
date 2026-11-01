package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.AsignacionDao;
import com.taller.nucleo.dao.jdbc.AsignacionDaoJdbc;
import com.taller.nucleo.dao.jdbc.TecnicoDaoJdbc;
import com.taller.nucleo.entidad.Asignacion;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.AccesoDenegadoException;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.seguridad.Accion;
import com.taller.nucleo.seguridad.Permisos;
import com.taller.nucleo.util.Transaccion;

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
}
