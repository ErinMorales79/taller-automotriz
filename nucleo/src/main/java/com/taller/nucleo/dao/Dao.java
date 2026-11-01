package com.taller.nucleo.dao;

import java.util.List;
import java.util.Optional;

/** Operaciones mínimas de persistencia de una entidad. */
public interface Dao<T> {

    /** Guarda la entidad nueva, le asigna su id y la devuelve. */
    T insertar(T entidad);

    Optional<T> buscarPorId(int id);

    List<T> listar();
}
