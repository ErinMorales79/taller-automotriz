package com.taller.nucleo.reporte;

import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.util.Validar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resultado de un reporte como tabla: título, nombres de columnas y filas de valores.
 * Es lo que la pantalla muestra y lo que los exportadores (PDF, Excel) escriben.
 */
public record Reporte(String titulo, List<String> columnas, List<List<Object>> filas) {

    public Reporte {
        titulo = Validar.textoObligatorio(titulo, "título del reporte", 150);
        if (columnas == null || columnas.isEmpty()) {
            throw new ReglaNegocioException("El reporte debe tener al menos una columna.");
        }
        Validar.obligatorio(filas, "filas del reporte");
        for (List<Object> fila : filas) {
            if (fila.size() != columnas.size()) {
                throw new ReglaNegocioException("Una fila del reporte no coincide con sus columnas.");
            }
        }
        columnas = List.copyOf(columnas);
        // Las celdas pueden ser null (por ejemplo, una orden aún sin fecha de entrega).
        List<List<Object>> copia = new ArrayList<>();
        for (List<Object> fila : filas) {
            copia.add(Collections.unmodifiableList(new ArrayList<>(fila)));
        }
        filas = Collections.unmodifiableList(copia);
    }

    public boolean estaVacio() {
        return filas.isEmpty();
    }
}
