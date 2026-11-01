package com.taller.nucleo.dao;

import com.taller.nucleo.entidad.Repuesto;

import java.util.List;

public interface RepuestoDao extends DaoModificable<Repuesto> {

    /** Busca por código o nombre que contengan el texto (vacío = todos). */
    List<Repuesto> buscar(String texto);

    List<Repuesto> listarStockBajo();

    /**
     * Suma (o resta, si es negativo) al stock de forma atómica en la base de datos.
     * Devuelve false, sin tocar nada, si el resultado quedaría por debajo de cero.
     */
    boolean ajustarStock(int idRepuesto, int variacion);
}
