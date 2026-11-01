package com.taller.nucleo.entidad;

import com.taller.nucleo.util.Validar;

public final class Tecnico extends Persona {

    private String especialidad;
    private boolean activo = true;

    public Tecnico(String nombre) {
        super(nombre);
    }

    @Override
    public String descripcion() {
        return especialidad == null ? getNombre() : getNombre() + " - " + especialidad;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = Validar.textoOpcional(especialidad, "especialidad", 60);
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
