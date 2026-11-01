package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.ConsumoRepuesto;

import java.util.List;

public interface ConsumoRepuestoDao extends Dao<ConsumoRepuesto> {

    List<ConsumoRepuesto> listarPorOrden(int idOrden);
}
