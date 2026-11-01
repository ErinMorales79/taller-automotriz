package com.taller.nucleo.entidad;

import com.taller.nucleo.util.Validar;

import java.time.LocalDateTime;

public final class ObservacionOrden {

    private Integer id;
    private final OrdenServicio orden;
    private final Tecnico tecnico;
    private final String texto;
    private LocalDateTime fecha = LocalDateTime.now();

    public ObservacionOrden(OrdenServicio orden, Tecnico tecnico, String texto) {
        this.orden = Validar.obligatorio(orden, "orden");
        this.tecnico = Validar.obligatorio(tecnico, "técnico");
        this.texto = Validar.textoObligatorio(texto, "observación", 5000);
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

    public String getTexto() {
        return texto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = Validar.obligatorio(fecha, "fecha");
    }
}
