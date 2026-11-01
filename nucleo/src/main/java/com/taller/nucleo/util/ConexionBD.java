package com.taller.nucleo.util;

import com.taller.nucleo.excepcion.PersistenciaException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * Entrega conexiones a MySQL. Los datos de acceso se leen de config/conexion.properties,
 * que no se sube a Git (ver config/conexion.properties.example).
 */
public final class ConexionBD {

    private static final String ARCHIVO = "config/conexion.properties";

    private static Supplier<Connection> proveedor;

    private ConexionBD() {
    }

    /** Solo para pruebas: reemplaza el origen de conexiones. Con null se restablece el original. */
    public static void fijarProveedor(Supplier<Connection> nuevo) {
        proveedor = nuevo;
    }

    /** Quien llama es responsable de cerrar la conexión (try-with-resources). */
    public static Connection obtener() {
        if (proveedor != null) {
            return proveedor.get();
        }
        Properties datos = cargarDatos();
        try {
            return DriverManager.getConnection(
                    datos.getProperty("url"),
                    datos.getProperty("usuario"),
                    datos.getProperty("contrasena"));
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar con la base de datos.", e);
        }
    }

    // shortcut: busca el archivo solo en la carpeta actual y en la superior; suficiente
    // para ejecutar desde la raíz o desde un módulo de Gradle.
    private static Properties cargarDatos() {
        Path ruta = Path.of(ARCHIVO);
        if (!Files.exists(ruta)) {
            ruta = Path.of("..", ARCHIVO);
        }
        if (!Files.exists(ruta)) {
            throw new PersistenciaException(
                    "Falta el archivo " + ARCHIVO + ". Copie conexion.properties.example y complételo.");
        }
        Properties datos = new Properties();
        try (InputStream entrada = Files.newInputStream(ruta)) {
            datos.load(entrada);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer el archivo " + ARCHIVO + ".", e);
        }
        return datos;
    }
}
