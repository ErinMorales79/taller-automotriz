package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.Auditoria;

import java.util.List;

/** La auditoría solo se agrega y se consulta. */
public interface AuditoriaDao extends Dao<Auditoria> {

    List<Auditoria> listarPorUsuario(int idUsuario);

    /** Registros de una tabla, el más reciente primero. */
    List<Auditoria> listarPorTabla(String tablaAfectada);
}
