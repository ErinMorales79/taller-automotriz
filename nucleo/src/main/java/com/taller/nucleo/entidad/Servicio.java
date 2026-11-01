package com.taller.nucleo.entidad;

import com.taller.nucleo.util.Validar;

import java.math.BigDecimal;

/** Servicio de mano de obra que el taller ofrece (con su precio base por hora). */
public final class Servicio {

    private Integer id;
    private String nombre;
    private BigDecimal precioBase;

    public Servicio(String nombre, BigDecimal precioBase) {
        setNombre(nombre);
        setPrecioBase(precioBase);
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
        this.nombre = Validar.textoObligatorio(nombre, "nombre del servicio", 100);
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = Validar.montoNoNegativo(precioBase, "precio base");
    }

    @Override
    public String toString() {
        return nombre;
    }
}
