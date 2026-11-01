package com.taller.nucleo.entidad;

import com.taller.nucleo.excepcion.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntidadesTest {

    private Vehiculo vehiculoValido() {
        Cliente cliente = new Cliente("1234567", "Ana Pérez");
        Modelo modelo = new Modelo(new Marca("Toyota"), "Corolla");
        return new Vehiculo("abc123", cliente, modelo, 2018);
    }

    @Test
    void clienteValidoSeCreaYSeDescribe() {
        Cliente cliente = new Cliente(" 1234567 ", " Ana Pérez ");
        assertEquals("Ana Pérez (1234567)", cliente.descripcion());
    }

    @Test
    void clienteSinNombreEsRechazado() {
        assertThrows(ReglaNegocioException.class, () -> new Cliente("1234567", "  "));
    }

    @Test
    void correoValidoSeAceptaEInvalidoSeRechaza() {
        Cliente cliente = new Cliente("1234567", "Ana Pérez");
        cliente.setCorreo("ana@correo.com");
        assertEquals("ana@correo.com", cliente.getCorreo());
        assertThrows(ReglaNegocioException.class, () -> cliente.setCorreo("sin-arroba"));
    }

    @Test
    void personasSePresentanSegunSuTipo() {
        Persona tecnico = new Tecnico("Luis Gómez");
        ((Tecnico) tecnico).setEspecialidad("Frenos");
        Persona cliente = new Cliente("99", "Ana");
        assertEquals("Luis Gómez - Frenos", tecnico.descripcion());
        assertEquals("Ana (99)", cliente.descripcion());
    }

    @Test
    void tecnicoNuevoEstaActivo() {
        assertTrue(new Tecnico("Luis").isActivo());
    }

    @Test
    void vehiculoGuardaLaPlacaEnMayusculas() {
        assertEquals("ABC123", vehiculoValido().getPlaca());
    }

    @Test
    void vehiculoRechazaAnioAnteriorA1950() {
        Vehiculo vehiculo = vehiculoValido();
        assertThrows(ReglaNegocioException.class, () -> vehiculo.setAnio(1949));
    }

    @Test
    void vehiculoRechazaKilometrajeNegativo() {
        Vehiculo vehiculo = vehiculoValido();
        assertThrows(ReglaNegocioException.class, () -> vehiculo.setKilometraje(-1));
    }

    @Test
    void vehiculoRechazaPropietarioNulo() {
        Modelo modelo = new Modelo(new Marca("Toyota"), "Corolla");
        assertThrows(ReglaNegocioException.class, () -> new Vehiculo("ABC123", null, modelo, 2018));
    }

    @Test
    void soloUnTecnicoSeVinculaAUsuarioConRolTecnico() {
        Usuario tecnico = new Usuario("luis", "hash", Rol.TECNICO);
        tecnico.setTecnico(new Tecnico("Luis"));
        Usuario recepcion = new Usuario("rosa", "hash", Rol.RECEPCIONISTA);
        assertThrows(ReglaNegocioException.class, () -> recepcion.setTecnico(new Tecnico("Luis")));
        assertNull(recepcion.getTecnico());
    }

    @Test
    void soloElAdministradorAutorizaExcepciones() {
        assertTrue(new Usuario("admin", "hash", Rol.ADMINISTRADOR).puedeAutorizarExcepciones());
        assertEquals(false, new Usuario("rosa", "hash", Rol.RECEPCIONISTA).puedeAutorizarExcepciones());
    }
}
