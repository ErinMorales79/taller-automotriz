package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.Usuario;

import java.util.Optional;

public interface UsuarioDao extends DaoModificable<Usuario> {

    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario);
}
