package com.taller.nucleo.entidad;

import com.taller.nucleo.util.Validar;

public final class Modelo {

    private Integer id;
    private Marca marca;
    private String nombre;

    public Modelo(Marca marca, String nombre) {
        setMarca(marca);
        setNombre(nombre);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = Validar.obligatorio(marca, "marca");
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = Validar.textoObligatorio(nombre, "nombre del modelo", 60);
    }

    @Override
    public String toString() {
        return marca + " " + nombre;
    }
}
