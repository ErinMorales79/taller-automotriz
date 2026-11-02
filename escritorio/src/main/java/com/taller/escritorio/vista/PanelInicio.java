package com.taller.escritorio.vista;

import com.taller.escritorio.Sesion;
import com.taller.escritorio.controlador.ControladorInicio;
import com.taller.escritorio.controlador.ControladorInicio.Resumen;
import com.taller.escritorio.vista.componentes.Encabezado;
import com.taller.escritorio.vista.componentes.TablaDatos;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.escritorio.vista.componentes.Tarjeta;
import com.taller.escritorio.vista.componentes.Tema;
import com.taller.nucleo.entidad.OrdenServicio;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

/** Pantalla de bienvenida con los números principales del taller y las órdenes más recientes. */
public final class PanelInicio extends JPanel {

    private final JPanel tarjetas = new JPanel(new GridLayout(1, 4, 16, 0));
    private final TablaDatos<OrdenServicio> recientes = new TablaDatos<>(PanelOrdenes.columnas());

    public PanelInicio() {
        super(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(new Encabezado("Hola, " + Sesion.usuario().getNombreUsuario(), "Este es el resumen del taller de hoy"),
                BorderLayout.NORTH);

        tarjetas.setOpaque(false);
        JLabel titulo = new JLabel("Órdenes recientes");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        JPanel abajo = new JPanel(new BorderLayout());
        abajo.setOpaque(false);
        abajo.add(titulo, BorderLayout.NORTH);
        abajo.add(recientes, BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);
        centro.add(tarjetas, BorderLayout.NORTH);
        centro.add(abajo, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
        new ControladorInicio(this).cargar();
    }

    public void mostrar(Resumen resumen) {
        tarjetas.removeAll();
        tarjetas.add(Tarjeta.estadistica("Órdenes activas", String.valueOf(resumen.activas()), Tema.PRIMARIO));
        tarjetas.add(Tarjeta.estadistica("Esperando aprobación", String.valueOf(resumen.esperandoAprobacion()), Tema.ADVERTENCIA));
        tarjetas.add(Tarjeta.estadistica("En reparación", String.valueOf(resumen.enReparacion()), Tema.EXITO));
        tarjetas.add(Tarjeta.estadistica("Repuestos con stock bajo", String.valueOf(resumen.stockBajo()),
                resumen.stockBajo() > 0 ? Tema.PELIGRO : Tema.TEXTO_SUAVE));
        tarjetas.revalidate();
        recientes.setDatos(resumen.recientes());
    }
}
