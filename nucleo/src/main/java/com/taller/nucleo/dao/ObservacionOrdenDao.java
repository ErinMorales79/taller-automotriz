package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.ObservacionOrden;

import java.util.List;

public interface ObservacionOrdenDao extends Dao<ObservacionOrden> {

    List<ObservacionOrden> listarPorOrden(int idOrden);
}
