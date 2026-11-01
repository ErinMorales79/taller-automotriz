package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.excepcion.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Ayudas comunes de los DAO: siempre consultas preparadas, y los errores de MySQL se
 * traducen a PersistenciaException con un mensaje claro en español.
 */
final class Jdbc {

    private static final int ERROR_DUPLICADO = 1062;
    private static final int ERROR_HIJOS_EXISTEN = 1451;
    private static final int ERROR_PADRE_NO_EXISTE = 1452;
    private static final int ERROR_CHECK = 3819;

    @FunctionalInterface
    interface Mapeador<T> {
        T mapear(ResultSet fila) throws SQLException;
    }

    private Jdbc() {
    }

    static <T> List<T> consultar(Connection conexion, String sql, Mapeador<T> mapeador, Object... parametros) {
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            asignar(sentencia, parametros);
            try (ResultSet filas = sentencia.executeQuery()) {
                List<T> resultado = new ArrayList<>();
                while (filas.next()) {
                    resultado.add(mapeador.mapear(filas));
                }
                return resultado;
            }
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    static <T> Optional<T> uno(Connection conexion, String sql, Mapeador<T> mapeador, Object... parametros) {
        return consultar(conexion, sql, mapeador, parametros).stream().findFirst();
    }

    /** Ejecuta un INSERT y devuelve el id generado. */
    static int insertar(Connection conexion, String sql, Object... parametros) {
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(sentencia, parametros);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    /** Ejecuta un UPDATE o DELETE y devuelve las filas afectadas. */
    static int ejecutar(Connection conexion, String sql, Object... parametros) {
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            asignar(sentencia, parametros);
            return sentencia.executeUpdate();
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    /** Como ejecutar, pero falla si no existía la fila a modificar. */
    static void ejecutarExigiendoFila(Connection conexion, String sql, Object... parametros) {
        if (ejecutar(conexion, sql, parametros) == 0) {
            throw new PersistenciaException("No existe el registro que se intentó modificar.");
        }
    }

    /** Para referencias que, por la clave foránea, tienen que existir. */
    static <T> T requerido(Optional<T> valor, String descripcion) {
        return valor.orElseThrow(() -> new PersistenciaException("No se encontró " + descripcion + "."));
    }

    static LocalDateTime fecha(ResultSet fila, String columna) throws SQLException {
        return fila.getObject(columna, LocalDateTime.class);
    }

    /** Lee un entero que puede ser NULL. */
    static Integer enteroOpcional(ResultSet fila, String columna) throws SQLException {
        int valor = fila.getInt(columna);
        return fila.wasNull() ? null : valor;
    }

    /** Patrón LIKE para búsquedas por texto (vacío = todo). */
    static String parcial(String texto) {
        return "%" + (texto == null ? "" : texto.trim()) + "%";
    }

    private static void asignar(PreparedStatement sentencia, Object[] parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            Object valor = parametros[i] instanceof Enum<?> e ? e.name() : parametros[i];
            sentencia.setObject(i + 1, valor);
        }
    }

    private static PersistenciaException traducir(SQLException e) {
        String mensaje = switch (e.getErrorCode()) {
            case ERROR_DUPLICADO -> "Ya existe un registro con ese valor, que debe ser único.";
            case ERROR_HIJOS_EXISTEN -> "No se puede eliminar: otros registros dependen de este.";
            case ERROR_PADRE_NO_EXISTE -> "Falta un registro relacionado (referencia inexistente).";
            case ERROR_CHECK -> "Un valor no cumple las restricciones de la base de datos.";
            default -> "Error al acceder a la base de datos.";
        };
        return new PersistenciaException(mensaje, e);
    }
}
