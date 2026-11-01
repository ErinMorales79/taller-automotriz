package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.Tecnico;

import java.util.List;

public interface TecnicoDao extends DaoModificable<Tecnico> {

    List<Tecnico> listarActivos();
}
