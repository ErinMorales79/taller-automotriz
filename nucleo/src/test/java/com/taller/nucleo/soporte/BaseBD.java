package com.taller.nucleo.soporte;

import com.taller.nucleo.util.ConexionBD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.UUID;

/**
 * Base de las pruebas que usan MySQL. Todo se ejecuta sobre UNA conexión real dentro de una
 * transacción que se revierte al terminar, así la base queda intacta. Las conexiones que reciben
 * los DAO y los servicios son un envoltorio: close() y commit() no hacen nada definitivo y
 * rollback() vuelve al último "commit" simulado (un punto de guardado). Así también se puede
 * probar que un rollback realmente deshace lo escrito a medias.
 */
public abstract class BaseBD {

    private Connection real;
    private Savepoint puntoGuardado;

    @BeforeEach
    void abrirConexionDePrueba() throws SQLException {
        real = ConexionBD.obtener();
        real.setAutoCommit(false);
        puntoGuardado = real.setSavepoint();
        ConexionBD.fijarProveedor(this::envolver);
    }

    @AfterEach
    void revertirTodo() throws SQLException {
        ConexionBD.fijarProveedor(null);
        real.rollback();
        real.close();
    }

    /** Conexión de prueba para quien necesite pasársela directo a un DAO. */
    protected Connection conexion() {
        return envolver();
    }

    /** Texto único para no chocar con datos que ya existan en la base. */
    protected static String unico(String prefijo) {
        return prefijo + UUID.randomUUID().toString().substring(0, 8);
    }

    private Connection envolver() {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, metodo, argumentos) -> {
                    switch (metodo.getName()) {
                        case "close", "setAutoCommit" -> {
                            return null;
                        }
                        case "getAutoCommit" -> {
                            return false;
                        }
                        case "commit" -> {
                            puntoGuardado = real.setSavepoint();
                            return null;
                        }
                        case "rollback" -> {
                            real.rollback(puntoGuardado);
                            return null;
                        }
                        default -> {
                            try {
                                return metodo.invoke(real, argumentos);
                            } catch (InvocationTargetException e) {
                                throw e.getCause();
                            }
                        }
                    }
                });
    }
}
