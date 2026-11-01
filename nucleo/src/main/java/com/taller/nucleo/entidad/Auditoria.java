package com.taller.nucleo.entidad;

import com.taller.nucleo.util.Validar;

import java.time.LocalDateTime;

/** Registro de auditoría: quién hizo qué, sobre qué tabla y cuándo. */
public final class Auditoria {

    private Integer id;
    private final Usuario usuario;
    private final String accion;
    private final String tablaAfectada;
    private final Integer idRegistro;
    private LocalDateTime fecha = LocalDateTime.now();

    public Auditoria(Usuario usuario, String accion, String tablaAfectada, Integer idRegistro) {
        this.usuario = Validar.obligatorio(usuario, "usuario");
        this.accion = Validar.textoObligatorio(accion, "acción", 50);
        this.tablaAfectada = Validar.textoObligatorio(tablaAfectada, "tabla afectada", 50);
        this.idRegistro = idRegistro;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getAccion() {
        return accion;
    }

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public Integer getIdRegistro() {
        return idRegistro;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = Validar.obligatorio(fecha, "fecha");
    }
}
