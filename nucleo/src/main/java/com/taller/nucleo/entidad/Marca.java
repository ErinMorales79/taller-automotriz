package com.taller.nucleo.entidad;

import com.taller.nucleo.util.Validar;

public final class Marca {

    private Integer id;
    private String nombre;

    public Marca(String nombre) {
        setNombre(nombre);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = Validar.textoObligatorio(nombre, "nombre de la marca", 60);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
