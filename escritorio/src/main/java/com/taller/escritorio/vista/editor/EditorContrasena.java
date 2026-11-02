package com.taller.escritorio.vista.editor;

import com.taller.escritorio.vista.componentes.EditorEntidad;
import com.taller.escritorio.vista.componentes.Formulario;
import com.taller.nucleo.excepcion.ReglaNegocioException;

import javax.swing.JComponent;
import javax.swing.JPasswordField;

/** Cambio de la contraseña propia: pide la actual y la nueva dos veces. */
public final class EditorContrasena implements EditorEntidad<EditorContrasena.DatosContrasena> {

    public record DatosContrasena(String actual, String nueva) {
    }

    private final Formulario formulario = new Formulario();
    private final JPasswordField actual = formulario.agregar("Contraseña actual *", new JPasswordField(26));
    private final JPasswordField nueva = formulario.agregar("Contraseña nueva * (mínimo 8 caracteres)", new JPasswordField(26));
    private final JPasswordField confirmacion = formulario.agregar("Repite la contraseña nueva *", new JPasswordField(26));

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(DatosContrasena existente) {
        throw new UnsupportedOperationException("No se precarga una contraseña.");
    }

    @Override
    public DatosContrasena construir(DatosContrasena existente) {
        String nuevaEscrita = new String(nueva.getPassword());
        if (!nuevaEscrita.equals(new String(confirmacion.getPassword()))) {
            throw new ReglaNegocioException("Las dos contraseñas nuevas no coinciden.");
        }
        return new DatosContrasena(new String(actual.getPassword()), nuevaEscrita);
    }
}
