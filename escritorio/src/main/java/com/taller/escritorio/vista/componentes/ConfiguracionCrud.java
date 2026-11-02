package com.taller.escritorio.vista.componentes;

import com.taller.nucleo.seguridad.Accion;
import com.taller.nucleo.servicio.CrudServicio;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Todo lo que PanelCrud necesita saber de un catálogo: textos, columnas, servicio y formulario.
 * "permiso" es la acción que habilita los botones de crear, editar y eliminar.
 */
public record ConfiguracionCrud<T>(String titulo, String subtitulo, String singular, String icono,
                                   List<TablaDatos.Columna<T>> columnas, CrudServicio<T, ?> servicio,
                                   Supplier<EditorEntidad<T>> editor, Accion permiso, Function<T, String> nombre,
                                   Function<T, Integer> id) {
}
