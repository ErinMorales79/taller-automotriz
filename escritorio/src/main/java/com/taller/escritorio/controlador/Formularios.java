package com.taller.escritorio.controlador;

import com.taller.escritorio.util.Tarea;
import com.taller.escritorio.vista.componentes.DialogoFormulario;
import com.taller.escritorio.vista.componentes.EditorEntidad;
import com.taller.escritorio.vista.componentes.Mensajes;
import com.taller.nucleo.excepcion.ReglaNegocioException;

import java.awt.Window;
import java.util.function.Consumer;

/** Abre un formulario modal, valida lo escrito y ejecuta la operación del servicio en segundo plano. */
final class Formularios {

    private Formularios() {
    }

    static <T> void pedir(Window ventana, String titulo, String textoBoton, EditorEntidad<T> editor,
                          Consumer<T> operacion, Runnable alTerminar) {
        new DialogoFormulario(ventana, titulo, editor.componente(), textoBoton, dialogo -> {
            T datos;
            try {
                datos = editor.construir(null);
            } catch (ReglaNegocioException e) {
                Mensajes.avisar(dialogo, e.getMessage());
                return;
            }
            Tarea.ejecutar(dialogo, () -> operacion.accept(datos), () -> {
                dialogo.cerrar();
                alTerminar.run();
            });
        }).setVisible(true);
    }
}
