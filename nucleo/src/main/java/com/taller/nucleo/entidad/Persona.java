package com.taller.nucleo.entidad;

import com.taller.nucleo.util.Validar;

/** Base común de las personas que intervienen en el taller (clientes y técnicos). */
public abstract class Persona {

    private static final int MAXIMO_NOMBRE = 100;

    private Integer id;
    private String nombre;

    protected Persona(String nombre) {
        setNombre(nombre);
    }

    /** Cada tipo de persona se presenta a su manera (polimorfismo). */
    public abstract String descripcion();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        this.nombre = Validar.textoObligatorio(nombre, "nombre", MAXIMO_NOMBRE);
    }

    @Override
    public String toString() {
        return descripcion();
    }
}
