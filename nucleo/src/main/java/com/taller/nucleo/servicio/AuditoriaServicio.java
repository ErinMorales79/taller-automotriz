package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.jdbc.AuditoriaDaoJdbc;
import com.taller.nucleo.entidad.Auditoria;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.seguridad.Accion;
import com.taller.nucleo.seguridad.Permisos;
import com.taller.nucleo.util.Transaccion;

import java.sql.Connection;
import java.util.List;

/**
 * Auditoría básica: quién hizo qué y cuándo. Los servicios registran dentro de su propia
 * transacción, así una acción y su registro se guardan juntos o no se guardan.
 */
public final class AuditoriaServicio {

    static void registrar(Connection conexion, Usuario usuario, String accion, String tabla, Integer idRegistro) {
        new AuditoriaDaoJdbc(conexion).insertar(new Auditoria(usuario, accion, tabla, idRegistro));
    }

    public List<Auditoria> consultarPorUsuario(Usuario solicitante, int idUsuario) {
        Permisos.exigir(solicitante, Accion.VER_AUDITORIA);
        return Transaccion.devolviendo(conexion -> new AuditoriaDaoJdbc(conexion).listarPorUsuario(idUsuario));
    }

    public List<Auditoria> consultarPorTabla(Usuario solicitante, String tabla) {
        Permisos.exigir(solicitante, Accion.VER_AUDITORIA);
        return Transaccion.devolviendo(conexion -> new AuditoriaDaoJdbc(conexion).listarPorTabla(tabla));
    }
}
