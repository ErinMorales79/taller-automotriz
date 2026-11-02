package com.taller.escritorio.controlador;

import com.taller.escritorio.Sesion;
import com.taller.escritorio.util.Tarea;
import com.taller.escritorio.vista.PanelCrud;
import com.taller.escritorio.vista.componentes.ConfiguracionCrud;
import com.taller.escritorio.vista.componentes.DialogoFormulario;
import com.taller.escritorio.vista.componentes.EditorEntidad;
import com.taller.escritorio.vista.componentes.Mensajes;
import com.taller.nucleo.excepcion.ReglaNegocioException;

/** Controlador de las pantallas de catálogo: delega todo en el CrudServicio, que valida permisos y audita. */
public final class ControladorCrud<T> {

    private final PanelCrud<T> vista;
    private final ConfiguracionCrud<T> configuracion;

    public ControladorCrud(PanelCrud<T> vista, ConfiguracionCrud<T> configuracion) {
        this.vista = vista;
        this.configuracion = configuracion;
    }

    public void cargar() {
        Tarea.ejecutar(vista, () -> configuracion.servicio().listar(), vista::mostrar);
    }

    public void nuevo() {
        abrirFormulario(null);
    }

    public void editar(T registro) {
        if (registro != null) {
            abrirFormulario(registro);
        }
    }

    public void eliminar(T registro) {
        if (registro == null || !Mensajes.confirmar(vista,
                "¿Eliminar " + configuracion.singular() + " \"" + configuracion.nombre().apply(registro) + "\"?")) {
            return;
        }
        Tarea.ejecutar(vista, () -> {
            configuracion.servicio().eliminar(Sesion.usuario(), configuracion.id().apply(registro));
        }, this::cargar);
    }

    private void abrirFormulario(T existente) {
        EditorEntidad<T> editor = configuracion.editor().get();
        if (existente != null) {
            editor.cargar(existente);
        }
        String titulo = (existente == null ? "Nuevo " : "Editar ") + configuracion.singular();
        new DialogoFormulario(vista.ventana(), titulo, editor.componente(), "Guardar",
                dialogo -> guardar(editor, existente, dialogo)).setVisible(true);
    }

    private void guardar(EditorEntidad<T> editor, T existente, DialogoFormulario dialogo) {
        T registro;
        try {
            registro = editor.construir(existente);
        } catch (ReglaNegocioException e) {
            Mensajes.avisar(dialogo, e.getMessage());
            return;
        }
        Tarea.ejecutar(dialogo, () -> {
            if (existente == null) {
                configuracion.servicio().crear(Sesion.usuario(), registro);
            } else {
                configuracion.servicio().actualizar(Sesion.usuario(), registro);
            }
        }, () -> {
            dialogo.cerrar();
            cargar();
        });
    }
}
