package com.taller.nucleo.entidad;

import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.util.Validar;

public final class Cliente extends Persona {

    private String documento;
    private String telefono;
    private String correo;
    private String direccion;

    public Cliente(String documento, String nombre) {
        super(nombre);
        setDocumento(documento);
    }

    @Override
    public String descripcion() {
        return getNombre() + " (" + documento + ")";
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = Validar.textoObligatorio(documento, "documento", 20);
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = Validar.textoOpcional(telefono, "teléfono", 20);
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        String limpio = Validar.textoOpcional(correo, "correo", 100);
        if (limpio != null && !limpio.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new ReglaNegocioException("El correo no tiene un formato válido.");
        }
        this.correo = limpio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = Validar.textoOpcional(direccion, "dirección", 150);
    }
}
