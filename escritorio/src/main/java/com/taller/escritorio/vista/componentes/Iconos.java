package com.taller.escritorio.vista.componentes;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.Icon;
import java.awt.Color;

/** Iconos SVG (carpeta resources/iconos) que se pintan del color pedido. */
public final class Iconos {

    private static final int TAMANO = 18;

    private Iconos() {
    }

    public static Icon de(String nombre) {
        return de(nombre, Color.DARK_GRAY, TAMANO);
    }

    public static Icon de(String nombre, Color color) {
        return de(nombre, color, TAMANO);
    }

    public static Icon de(String nombre, Color color, int tamano) {
        FlatSVGIcon icono = new FlatSVGIcon("iconos/" + nombre + ".svg", tamano, tamano);
        icono.setColorFilter(new FlatSVGIcon.ColorFilter(original -> color));
        return icono;
    }
}
