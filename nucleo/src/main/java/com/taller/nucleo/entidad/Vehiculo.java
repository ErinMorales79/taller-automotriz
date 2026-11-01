package com.taller.nucleo.entidad;

import com.taller.nucleo.util.Validar;

import java.util.Locale;

public final class Vehiculo {

    private static final int ANIO_MINIMO = 1950;

    private Integer id;
    private String placa;
    private Cliente propietario;
    private Modelo modelo;
    private int anio;
    private String color;
    private int kilometraje;

    public Vehiculo(String placa, Cliente propietario, Modelo modelo, int anio) {
        setPlaca(placa);
        setPropietario(propietario);
        setModelo(modelo);
        setAnio(anio);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = Validar.textoObligatorio(placa, "placa", 15).toUpperCase(Locale.ROOT);
    }

    public Cliente getPropietario() {
        return propietario;
    }

    public void setPropietario(Cliente propietario) {
        this.propietario = Validar.obligatorio(propietario, "propietario");
    }

    public Modelo getModelo() {
        return modelo;
    }

    public void setModelo(Modelo modelo) {
        this.modelo = Validar.obligatorio(modelo, "modelo");
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = Validar.enteroMinimo(anio, ANIO_MINIMO, "año");
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = Validar.textoOpcional(color, "color", 30);
    }

    public int getKilometraje() {
        return kilometraje;
    }

    public void setKilometraje(int kilometraje) {
        this.kilometraje = Validar.enteroMinimo(kilometraje, 0, "kilometraje");
    }

    @Override
    public String toString() {
        return placa + " - " + modelo;
    }
}
