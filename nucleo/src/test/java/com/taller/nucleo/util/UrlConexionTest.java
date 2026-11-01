package com.taller.nucleo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UrlConexionTest {

    @Test
    void reemplazaSoloElNombreDeLaBaseYConservaLosParametros() {
        assertEquals("jdbc:mysql://localhost:3306/otra?useSSL=false&serverTimezone=UTC",
                ConexionBD.urlFinal("jdbc:mysql://localhost:3306/taller_automotriz?useSSL=false&serverTimezone=UTC", "otra"));
    }

    @Test
    void funcionaSinParametros() {
        assertEquals("jdbc:mysql://servidor/otra", ConexionBD.urlFinal("jdbc:mysql://servidor/taller", "otra"));
    }

    @Test
    void sinNombreDeBaseEnLaUrlNoLaDanaNiInventaNada() {
        assertEquals("jdbc:mysql://servidor:3306", ConexionBD.urlFinal("jdbc:mysql://servidor:3306", "otra"));
        assertEquals("jdbc:mysql://servidor:3306/?x=1", ConexionBD.urlFinal("jdbc:mysql://servidor:3306/?x=1", "otra"));
    }

    @Test
    void sinNombreConfiguradoLaUrlQuedaIgual() {
        assertEquals("jdbc:mysql://h/b?x=1", ConexionBD.urlFinal("jdbc:mysql://h/b?x=1", null));
        assertNull(ConexionBD.urlFinal(null, "otra"));
    }
}
