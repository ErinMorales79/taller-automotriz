package com.taller.escritorio.util;

import com.taller.escritorio.vista.componentes.Mensajes;
import com.taller.nucleo.excepcion.ReglaNegocioException;

import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Window;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;

/**
 * Ejecuta el trabajo pesado (base de datos) fuera del hilo de la interfaz para que la ventana no
 * se congele, y muestra al usuario cualquier error con un mensaje claro en español.
 */
public final class Tarea {

    /** Las pruebas lo activan para ejecutar todo en el mismo hilo. */
    private static boolean sincrona;

    private Tarea() {
    }

    public static void usarModoSincrono(boolean valor) {
        sincrona = valor;
    }

    public static <R> void ejecutar(Component padre, Supplier<R> trabajo, Consumer<R> alTerminar) {
        if (sincrona) {
            alTerminar.accept(trabajo.get());
            return;
        }
        Window ventana = padre == null ? null : SwingUtilities.getWindowAncestor(padre);
        if (ventana != null) {
            ventana.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        new SwingWorker<R, Void>() {
            @Override
            protected R doInBackground() {
                return trabajo.get();
            }

            @Override
            protected void done() {
                if (ventana != null) {
                    ventana.setCursor(Cursor.getDefaultCursor());
                }
                try {
                    alTerminar.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (java.util.concurrent.ExecutionException e) {
                    mostrar(padre, e.getCause());
                }
            }
        }.execute();
    }

    public static void ejecutar(Component padre, Runnable trabajo, Runnable alTerminar) {
        ejecutar(padre, () -> {
            trabajo.run();
            return Boolean.TRUE;
        }, resultado -> alTerminar.run());
    }

    /** Las reglas de negocio avisan; todo lo demás es un error. */
    public static void mostrar(Component padre, Throwable causa) {
        if (causa instanceof ReglaNegocioException) {
            Mensajes.avisar(padre, causa.getMessage());
        } else if (causa instanceof com.taller.nucleo.excepcion.PersistenciaException
                || causa instanceof com.taller.nucleo.excepcion.ExportacionException) {
            Mensajes.error(padre, causa.getMessage());
        } else {
            causa.printStackTrace();
            Mensajes.error(padre, "Ocurrió un error inesperado: " + causa.getMessage());
        }
    }
}
