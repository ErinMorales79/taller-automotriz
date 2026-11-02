package com.taller.escritorio.vista.componentes;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.util.Map;

/** Apariencia común de toda la aplicación: colores, tipografía y ajustes de FlatLaf. */
public final class Tema {

    public static final Color PRIMARIO = new Color(0x2563EB);
    public static final Color PRIMARIO_OSCURO = new Color(0x1D4ED8);
    public static final Color PELIGRO = new Color(0xDC2626);
    public static final Color EXITO = new Color(0x16A34A);
    public static final Color ADVERTENCIA = new Color(0xD97706);
    public static final Color MENU_FONDO = new Color(0x0F172A);
    public static final Color MENU_ACTIVO = new Color(0x1E293B);
    public static final Color MENU_TEXTO = new Color(0xCBD5E1);
    public static final Color FONDO = new Color(0xF1F5F9);
    public static final Color TARJETA = Color.WHITE;
    public static final Color BORDE = new Color(0xE2E8F0);
    public static final Color TEXTO_SUAVE = new Color(0x64748B);

    private Tema() {
    }

    public static void instalar() {
        FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#2563EB"));
        FlatLightLaf.setup();
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("Table.rowHeight", 32);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 1));
        UIManager.put("Table.gridColor", BORDE);
        UIManager.put("TableHeader.height", 36);
        UIManager.put("TableHeader.background", new Color(0xF8FAFC));
        UIManager.put("TableHeader.bottomSeparatorColor", BORDE);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("OptionPane.yesButtonText", "Sí");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        UIManager.put("OptionPane.okButtonText", "Aceptar");
    }
}
