package com.taller.nucleo.entidad;

import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.util.Validar;

public final class Usuario {

    private Integer id;
    private String nombreUsuario;
    private String contrasenaHash;
    private Rol rol;
    private boolean activo = true;
    private Tecnico tecnico;

    /** La contraseña debe llegar ya convertida en hash BCrypt, nunca en texto plano. */
    public Usuario(String nombreUsuario, String contrasenaHash, Rol rol) {
        setNombreUsuario(nombreUsuario);
        setContrasenaHash(contrasenaHash);
        this.rol = Validar.obligatorio(rol, "rol");
    }

    public boolean puedeAutorizarExcepciones() {
        return rol == Rol.ADMINISTRADOR;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = Validar.textoObligatorio(nombreUsuario, "nombre de usuario", 50);
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = Validar.textoObligatorio(contrasenaHash, "contraseña", 100);
    }

    public Rol getRol() {
        return rol;
    }

    /** Cambia el rol manteniendo la regla del esquema: solo un TECNICO se vincula a un técnico. */
    public void asignarRol(Rol nuevoRol, Tecnico nuevoTecnico) {
        Validar.obligatorio(nuevoRol, "rol");
        if (nuevoRol == Rol.TECNICO && nuevoTecnico == null) {
            throw new ReglaNegocioException("Un usuario con rol TECNICO debe vincularse a un técnico.");
        }
        this.rol = nuevoRol;
        this.tecnico = nuevoRol == Rol.TECNICO ? nuevoTecnico : null;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Tecnico getTecnico() {
        return tecnico;
    }

    /** Solo los usuarios con rol TECNICO se vinculan a un técnico (igual que el esquema). */
    public void setTecnico(Tecnico tecnico) {
        if (tecnico != null && rol != Rol.TECNICO) {
            throw new ReglaNegocioException("Solo un usuario con rol TECNICO puede vincularse a un técnico.");
        }
        this.tecnico = tecnico;
    }

    @Override
    public String toString() {
        return nombreUsuario + " (" + rol + ")";
    }
}
