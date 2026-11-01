package com.taller.nucleo.entidad;

import com.taller.nucleo.util.Validar;

import java.time.LocalDateTime;

public final class Diagnostico {

    private Integer id;
    private final OrdenServicio orden;
    private final Tecnico tecnico;
    private String descripcion;
    private LocalDateTime fecha = LocalDateTime.now();

    public Diagnostico(OrdenServicio orden, Tecnico tecnico, String descripcion) {
        this.orden = Validar.obligatorio(orden, "orden");
        this.tecnico = Validar.obligatorio(tecnico, "técnico");
        setDescripcion(descripcion);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public OrdenServicio getOrden() {
        return orden;
    }

    public Tecnico getTecnico() {
        return tecnico;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = Validar.textoObligatorio(descripcion, "descripción del diagnóstico", 5000);
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = Validar.obligatorio(fecha, "fecha");
    }
}
