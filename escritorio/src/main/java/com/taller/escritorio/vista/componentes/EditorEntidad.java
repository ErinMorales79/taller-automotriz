package com.taller.escritorio.vista.componentes;

import javax.swing.JComponent;

/** Formulario para crear o editar un registro. Lo usa PanelCrud dentro de un DialogoFormulario. */
public interface EditorEntidad<T> {

    JComponent componente();

    /** Rellena los campos con el registro a editar. */
    void cargar(T existente);

    /**
     * Arma el registro con lo escrito. Si existente es null crea uno nuevo; si no, modifica ese mismo
     * objeto (así conserva su id). Lanza ReglaNegocioException si algún dato no es válido.
     */
    T construir(T existente);
}
