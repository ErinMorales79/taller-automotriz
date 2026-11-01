package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteDao extends DaoModificable<Cliente> {

    Optional<Cliente> buscarPorDocumento(String documento);

    /** Busca por documento, nombre o teléfono que contengan el texto (vacío = todos). */
    List<Cliente> buscar(String texto);
}
