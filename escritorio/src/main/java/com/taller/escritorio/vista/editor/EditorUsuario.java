package com.taller.escritorio.vista.editor;

import com.taller.escritorio.Servicios;
import com.taller.escritorio.vista.componentes.EditorEntidad;
import com.taller.escritorio.vista.componentes.Formulario;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.excepcion.ReglaNegocioException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Datos de un usuario nuevo o, con soloRol, el cambio de rol de uno existente.
 * El técnico solo se elige cuando el rol es TECNICO.
 */
public final class EditorUsuario implements EditorEntidad<EditorUsuario.DatosUsuario> {

    public record DatosUsuario(String nombreUsuario, String contrasena, Rol rol, Tecnico tecnico) {
    }

    private final Formulario formulario = new Formulario();
    private JTextField nombre;
    private JPasswordField contrasena;
    private JPasswordField confirmacion;
    private final JComboBox<Rol> rol = new JComboBox<>(Rol.values());
    private final JComboBox<Tecnico> tecnico = new JComboBox<>();
    private final boolean soloRol;

    public EditorUsuario(boolean soloRol) {
        this.soloRol = soloRol;
        if (!soloRol) {
            nombre = formulario.agregarTexto("Nombre de usuario *");
            contrasena = formulario.agregar("Contraseña * (mínimo 8 caracteres)", new JPasswordField(26));
            confirmacion = formulario.agregar("Repite la contraseña *", new JPasswordField(26));
        }
        formulario.agregar("Rol *", rol);
        formulario.agregar("Técnico (solo para el rol Técnico)", tecnico);
        Servicios.CONSULTAS.tecnicosActivos().forEach(tecnico::addItem);
        tecnico.setSelectedIndex(-1);
        rol.addActionListener(e -> tecnico.setEnabled(rol.getSelectedItem() == Rol.TECNICO));
        tecnico.setEnabled(rol.getSelectedItem() == Rol.TECNICO);
    }

    @Override
    public JComponent componente() {
        return formulario;
    }

    @Override
    public void cargar(DatosUsuario existente) {
        rol.setSelectedItem(existente.rol());
    }

    @Override
    public DatosUsuario construir(DatosUsuario existente) {
        if (!soloRol && !new String(contrasena.getPassword()).equals(new String(confirmacion.getPassword()))) {
            throw new ReglaNegocioException("Las dos contraseñas no coinciden.");
        }
        Rol rolElegido = (Rol) rol.getSelectedItem();
        Tecnico tecnicoElegido = rolElegido == Rol.TECNICO ? (Tecnico) tecnico.getSelectedItem() : null;
        if (rolElegido == Rol.TECNICO && tecnicoElegido == null) {
            throw new ReglaNegocioException("Un usuario con rol Técnico debe estar ligado a un técnico del taller.");
        }
        return new DatosUsuario(soloRol ? null : nombre.getText(), soloRol ? null : new String(contrasena.getPassword()),
                rolElegido, tecnicoElegido);
    }
}
