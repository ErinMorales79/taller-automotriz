package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.Diagnostico;

import java.util.List;

public interface DiagnosticoDao extends DaoModificable<Diagnostico> {

    List<Diagnostico> listarPorOrden(int idOrden);
}
