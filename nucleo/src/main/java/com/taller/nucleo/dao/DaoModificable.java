package com.taller.nucleo.dao;

/** Entidades que además se pueden editar y eliminar (CRUD completo). */
public interface DaoModificable<T> extends Dao<T> {

    void actualizar(T entidad);

    void eliminar(int id);
}
