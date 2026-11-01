package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.Servicio;

import java.util.List;

public interface ServicioDao extends DaoModificable<Servicio> {

    List<Servicio> buscar(String texto);
}
