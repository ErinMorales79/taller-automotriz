package com.taller.escritorio.util;

import com.taller.nucleo.excepcion.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormatoTest {

    @Test
    void elDineroSeMuestraConSeparadorDeMilesYDosDecimales() {
        assertEquals("1,234.50", Formato.dinero(new BigDecimal("1234.5")));
        assertEquals("-", Formato.dinero(null));
    }

    @Test
    void losEstadosSeLeenEnEspanolConAcentos() {
        assertEquals("En reparación", Formato.legible("EN_REPARACION"));
        assertEquals("Esperando aprobación", Formato.legible("ESPERANDO_APROBACION"));
        assertEquals("Ensayo", Formato.legible("ENSAYO"));
    }

    @Test
    void lasFechasTienenFormatoDiaMesAnio() {
        assertEquals("05/03/2026 14:30", Formato.fechaHora(LocalDateTime.of(2026, 3, 5, 14, 30)));
        assertEquals(LocalDate.of(2026, 1, 31), Formato.fechaOpcional("31/01/2026", "desde"));
        assertNull(Formato.fechaOpcional("  ", "desde"));
    }

    @Test
    void unaFechaMalEscritaDaUnMensajeClaro() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> Formato.fechaOpcional("2026-01-31", "desde"));
        assertEquals("El campo desde debe tener el formato dd/mm/aaaa, por ejemplo 31/01/2026.", error.getMessage());
    }

    @Test
    void losNumerosInvalidosSeRechazanConSuNombreDeCampo() {
        assertEquals(new BigDecimal("125.50"), Formato.decimal("125.50", "precio"));
        assertEquals(7, Formato.entero(" 7 ", "cantidad"));
        assertThrows(ReglaNegocioException.class, () -> Formato.decimal("abc", "precio"));
        assertThrows(ReglaNegocioException.class, () -> Formato.entero("1.5", "cantidad"));
        assertThrows(ReglaNegocioException.class, () -> Formato.entero(null, "cantidad"));
    }
}
