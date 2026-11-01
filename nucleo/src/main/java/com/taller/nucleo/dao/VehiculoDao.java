package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.Vehiculo;

import java.util.List;
import java.util.Optional;

public interface VehiculoDao extends DaoModificable<Vehiculo> {

    Optional<Vehiculo> buscarPorPlaca(String placa);

    List<Vehiculo> listarPorCliente(int idCliente);

    /** Busca por placa, color, o nombre del propietario que contengan el texto (vacío = todos). */
    List<Vehiculo> buscar(String texto);
}
