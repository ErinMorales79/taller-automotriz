package com.taller.nucleo.util;

import com.taller.nucleo.excepcion.PersistenciaException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Ejecuta un bloque de trabajo en una sola transacción: commit si termina bien,
 * rollback ante cualquier error. Los servicios la usan para operaciones encabezado-detalle
 * y movimientos de inventario.
 */
public final class Transaccion {

    private Transaccion() {
    }

    public static <T> T devolviendo(Function<Connection, T> trabajo) {
        try (Connection conexion = ConexionBD.obtener()) {
            conexion.setAutoCommit(false);
            try {
                T resultado = trabajo.apply(conexion);
                conexion.commit();
                return resultado;
            } catch (RuntimeException e) {
                revertir(conexion, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo completar la transacción.", e);
        }
    }

    public static void ejecutar(Consumer<Connection> trabajo) {
        devolviendo(conexion -> {
            trabajo.accept(conexion);
            return null;
        });
    }

    private static void revertir(Connection conexion, RuntimeException causa) {
        try {
            conexion.rollback();
        } catch (SQLException e) {
            causa.addSuppressed(e);
        }
    }
}
