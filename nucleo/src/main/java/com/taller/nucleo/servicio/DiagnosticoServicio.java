package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.jdbc.DiagnosticoDaoJdbc;
import com.taller.nucleo.dao.jdbc.TecnicoDaoJdbc;
import com.taller.nucleo.entidad.Diagnostico;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.estado.EnDiagnostico;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.seguridad.Accion;
import com.taller.nucleo.seguridad.Permisos;
import com.taller.nucleo.util.Transaccion;

/** Registro del diagnóstico de una orden: solo mientras la orden está EN_DIAGNOSTICO. */
public final class DiagnosticoServicio {

    public Diagnostico registrar(int idOrden, int idTecnico, String descripcion, Usuario responsable) {
        Permisos.exigir(responsable, Accion.GESTIONAR_ORDEN);
        return Transaccion.devolviendo(conexion -> {
            OrdenServicio orden = Consultas.orden(conexion, idOrden);
            if (!(orden.getEstado() instanceof EnDiagnostico)) {
                throw new ReglaNegocioException("Solo se puede registrar un diagnóstico mientras la orden está en diagnóstico.");
            }
            Tecnico tecnico = new TecnicoDaoJdbc(conexion).buscarPorId(idTecnico)
                    .orElseThrow(() -> new ReglaNegocioException("El técnico #" + idTecnico + " no existe."));
            Diagnostico diagnostico = new Diagnostico(orden, tecnico, descripcion);
            new DiagnosticoDaoJdbc(conexion).insertar(diagnostico);
            AuditoriaServicio.registrar(conexion, responsable, "REGISTRAR_DIAGNOSTICO", "diagnostico", diagnostico.getId());
            return diagnostico;
        });
    }
}
