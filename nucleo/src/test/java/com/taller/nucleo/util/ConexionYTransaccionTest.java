package com.taller.nucleo.util;

import com.taller.nucleo.excepcion.PersistenciaException;
import com.taller.nucleo.soporte.BaseBD;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConexionYTransaccionTest extends BaseBD {

    private static int contarMarcas(Connection conexion, String nombre) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                "SELECT COUNT(*) FROM marca WHERE nombre = ?")) {
            sentencia.setString(1, nombre);
            try (ResultSet fila = sentencia.executeQuery()) {
                fila.next();
                return fila.getInt(1);
            }
        }
    }

    private static void insertarMarca(Connection conexion, String nombre) {
        try (PreparedStatement sentencia = conexion.prepareStatement("INSERT INTO marca (nombre) VALUES (?)")) {
            sentencia.setString(1, nombre);
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Falló el insert de prueba.", e);
        }
    }

    @Test
    void laConexionALaBaseFunciona() throws SQLException {
        try (Connection conexion = ConexionBD.obtener()) {
            assertTrue(conexion.isValid(2));
        }
    }

    @Test
    void transaccionConfirmaCuandoTodoSaleBien() throws SQLException {
        String marca = unico("ok");
        Transaccion.ejecutar(conexion -> insertarMarca(conexion, marca));
        try (Connection conexion = ConexionBD.obtener()) {
            assertEquals(1, contarMarcas(conexion, marca));
        }
    }

    @Test
    void transaccionDeshaceTodoSiAlgoFalla() throws SQLException {
        String marca = unico("mal");
        assertThrows(IllegalStateException.class, () -> Transaccion.ejecutar(conexion -> {
            insertarMarca(conexion, marca);
            throw new IllegalStateException("falla a medias");
        }));
        try (Connection conexion = ConexionBD.obtener()) {
            assertEquals(0, contarMarcas(conexion, marca));
        }
    }
}
