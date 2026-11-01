package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.jdbc.OrdenServicioDaoJdbc;
import com.taller.nucleo.dao.jdbc.PresupuestoDaoJdbc;
import com.taller.nucleo.dao.jdbc.RepuestoDaoJdbc;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Presupuesto;
import com.taller.nucleo.entidad.Repuesto;
import com.taller.nucleo.excepcion.ReglaNegocioException;

import java.sql.Connection;

/** Cargas comunes de los servicios: si el registro no existe, error claro para el usuario. */
final class Consultas {

    private Consultas() {
    }

    static OrdenServicio orden(Connection conexion, int idOrden) {
        return new OrdenServicioDaoJdbc(conexion).buscarPorId(idOrden)
                .orElseThrow(() -> new ReglaNegocioException("La orden #" + idOrden + " no existe."));
    }

    static Presupuesto presupuesto(Connection conexion, int idPresupuesto) {
        return new PresupuestoDaoJdbc(conexion).buscarPorId(idPresupuesto)
                .orElseThrow(() -> new ReglaNegocioException("El presupuesto #" + idPresupuesto + " no existe."));
    }

    static Repuesto repuesto(Connection conexion, int idRepuesto) {
        return new RepuestoDaoJdbc(conexion).buscarPorId(idRepuesto)
                .orElseThrow(() -> new ReglaNegocioException("El repuesto #" + idRepuesto + " no existe."));
    }

    /** Un objeto del dominio debe estar guardado (tener id) antes de usarlo en una operación. */
    static int idGuardado(Integer id, String que) {
        if (id == null) {
            throw new ReglaNegocioException("Primero debe guardarse " + que + ".");
        }
        return id;
    }
}
