package com.taller.escritorio.vista.componentes;

import com.taller.escritorio.util.Formato;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/** Tabla estándar: muestra una lista de objetos, ordena por columna y filtra por texto. */
public final class TablaDatos<T> extends JScrollPane {

    public record Columna<T>(String titulo, Function<T, Object> valor) {
    }

    private static final Map<String, Color> COLORES_ESTADO = Map.ofEntries(
            Map.entry("RECIBIDA", new Color(0x2563EB)), Map.entry("EN_DIAGNOSTICO", new Color(0x7C3AED)),
            Map.entry("ESPERANDO_APROBACION", new Color(0xD97706)), Map.entry("EN_REPARACION", new Color(0x0891B2)),
            Map.entry("FINALIZADA", new Color(0x16A34A)), Map.entry("ENTREGADA", new Color(0x475569)),
            Map.entry("CANCELADA", new Color(0xDC2626)), Map.entry("APROBADO", new Color(0x16A34A)),
            Map.entry("RECHAZADO", new Color(0xDC2626)), Map.entry("PENDIENTE", new Color(0xD97706)));

    private final List<Columna<T>> columnas;
    private final List<T> filas = new ArrayList<>();
    private final JTable tabla = new JTable();
    private final TableRowSorter<AbstractTableModel> ordenador;

    public TablaDatos(List<Columna<T>> columnas) {
        this.columnas = List.copyOf(columnas);
        AbstractTableModel modelo = new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return filas.size();
            }

            @Override
            public int getColumnCount() {
                return TablaDatos.this.columnas.size();
            }

            @Override
            public String getColumnName(int columna) {
                return TablaDatos.this.columnas.get(columna).titulo();
            }

            @Override
            public Object getValueAt(int fila, int columna) {
                return TablaDatos.this.columnas.get(columna).valor().apply(filas.get(fila));
            }
        };
        tabla.setModel(modelo);
        ordenador = new TableRowSorter<>(modelo);
        tabla.setRowSorter(ordenador);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(tabla.getFont().deriveFont(Font.BOLD));
        tabla.setDefaultRenderer(Object.class, new Celda());
        setViewportView(tabla);
        setColumnHeaderView(tabla.getTableHeader());
        setBorder(javax.swing.BorderFactory.createLineBorder(Tema.BORDE));
        getViewport().setBackground(Color.WHITE);
    }

    public void setDatos(List<T> datos) {
        filas.clear();
        filas.addAll(datos);
        ((AbstractTableModel) tabla.getModel()).fireTableDataChanged();
        ajustarColumnas();
    }

    /** Cada columna toma el ancho de su texto más largo (con un tope) para que nada se vea cortado. */
    private void ajustarColumnas() {
        java.awt.FontMetrics medida = tabla.getFontMetrics(tabla.getFont().deriveFont(Font.BOLD));
        for (int c = 0; c < columnas.size(); c++) {
            int ancho = medida.stringWidth(columnas.get(c).titulo()) + 44;
            for (int f = 0; f < Math.min(filas.size(), 100); f++) {
                ancho = Math.max(ancho, medida.stringWidth(textoDe(columnas.get(c).valor().apply(filas.get(f)))) + 40);
            }
            tabla.getColumnModel().getColumn(c).setPreferredWidth(Math.min(ancho, 380));
        }
    }

    public int cantidadFilas() {
        return filas.size();
    }

    /** Muestra solo las filas que contienen el texto (sin distinguir mayúsculas) en alguna columna. */
    public void filtrar(String texto) {
        if (texto == null || texto.isBlank()) {
            ordenador.setRowFilter(null);
            return;
        }
        String buscado = texto.trim().toLowerCase(Locale.ROOT);
        ordenador.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entrada) {
                for (int c = 0; c < entrada.getValueCount(); c++) {
                    if (textoDe(entrada.getValue(c)).toLowerCase(Locale.ROOT).contains(buscado)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /** El objeto de la fila elegida, o null si no hay selección. */
    public T seleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : filas.get(tabla.convertRowIndexToModel(fila));
    }

    public void alElegir(Runnable accion) {
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                accion.run();
            }
        });
    }

    public void alDobleClic(Consumer<T> accion) {
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && seleccionado() != null) {
                    accion.accept(seleccionado());
                }
            }
        });
    }

    JTable tabla() {
        return tabla;
    }

    static String textoDe(Object valor) {
        if (valor == null) {
            return "-";
        }
        if (valor instanceof BigDecimal monto) {
            return Formato.dinero(monto);
        }
        if (valor instanceof LocalDateTime fecha) {
            return Formato.fechaHora(fecha);
        }
        if (valor instanceof Boolean si) {
            return si ? "Sí" : "No";
        }
        if (valor instanceof Enum<?> constante) {
            return Formato.legible(constante.name());
        }
        return valor.toString();
    }

    private static final class Celda extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean enfoque, int fila, int columna) {
            JLabel etiqueta = (JLabel) super.getTableCellRendererComponent(tabla, textoDe(valor), seleccionada, enfoque, fila, columna);
            etiqueta.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));
            etiqueta.setHorizontalAlignment(valor instanceof Number ? SwingConstants.RIGHT : SwingConstants.LEFT);
            etiqueta.setFont(tabla.getFont());
            String nombre = valor instanceof Enum<?> constante ? constante.name() : valor == null ? "" : valor.toString();
            Color color = COLORES_ESTADO.get(nombre);
            if (color != null) {
                etiqueta.setText(Formato.legible(nombre));
                etiqueta.setFont(tabla.getFont().deriveFont(Font.BOLD));
            }
            // El renderizador se reutiliza en todas las celdas: el color se fija siempre, nunca se hereda.
            etiqueta.setForeground(seleccionada ? tabla.getSelectionForeground()
                    : color != null ? color : tabla.getForeground());
            return etiqueta;
        }
    }
}
