package com.taller.escritorio.vista.componentes;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;

/** Fábrica de botones estándar: primario (acción principal), secundario y peligro. */
public final class Boton {

    private Boton() {
    }

    public static JButton primario(String texto, String icono) {
        return crear(texto, icono, Color.WHITE,
                "background:#2563EB; foreground:#FFFFFF; hoverBackground:#1D4ED8; pressedBackground:#1E40AF; "
                        + "borderWidth:0; focusWidth:0; arc:10; margin:6,14,6,14");
    }

    public static JButton secundario(String texto, String icono) {
        return crear(texto, icono, Tema.TEXTO_SUAVE,
                "background:#FFFFFF; foreground:#0F172A; borderColor:#CBD5E1; hoverBackground:#F1F5F9; "
                        + "focusWidth:0; arc:10; margin:6,14,6,14");
    }

    public static JButton peligro(String texto, String icono) {
        return crear(texto, icono, Tema.PELIGRO,
                "background:#FFFFFF; foreground:#DC2626; borderColor:#FCA5A5; hoverBackground:#FEF2F2; "
                        + "focusWidth:0; arc:10; margin:6,14,6,14");
    }

    private static JButton crear(String texto, String icono, Color colorIcono, String estilo) {
        JButton boton = new JButton(texto);
        if (icono != null) {
            boton.setIcon(Iconos.de(icono, colorIcono));
            boton.setIconTextGap(8);
        }
        boton.putClientProperty(FlatClientProperties.STYLE, estilo);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }
}
