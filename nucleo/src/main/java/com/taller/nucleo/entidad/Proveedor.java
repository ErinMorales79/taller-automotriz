package com.taller.nucleo.entidad;

import com.taller.nucleo.util.Validar;

public final class Proveedor {

    private Integer id;
    private String nombre;
    private String telefono;

    public Proveedor(String nombre) {
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
        this.nombre = Validar.textoObligatorio(nombre, "nombre del proveedor", 100);
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = Validar.textoOpcional(telefono, "teléfono", 20);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
