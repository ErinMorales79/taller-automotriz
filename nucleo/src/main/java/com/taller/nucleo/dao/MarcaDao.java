package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.Marca;

import java.util.Optional;

public interface MarcaDao extends DaoModificable<Marca> {

    Optional<Marca> buscarPorNombre(String nombre);
}
