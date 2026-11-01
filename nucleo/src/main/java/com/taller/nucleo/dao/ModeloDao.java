package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.Modelo;

import java.util.List;

public interface ModeloDao extends DaoModificable<Modelo> {

    List<Modelo> listarPorMarca(int idMarca);
}
