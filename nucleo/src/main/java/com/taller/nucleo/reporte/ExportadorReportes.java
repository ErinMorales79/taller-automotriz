package com.taller.nucleo.reporte;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.taller.nucleo.excepcion.ExportacionException;
import com.taller.nucleo.util.Validar;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Escribe un Reporte en PDF (OpenPDF) o en Excel (Apache POI). */
public final class ExportadorReportes {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int COLUMNAS_PARA_HOJA_HORIZONTAL = 6;
    private static final int LARGO_MAXIMO_NOMBRE_HOJA = 31;

    private ExportadorReportes() {
    }

    public static void aPdf(Reporte reporte, Path destino) {
        Validar.obligatorio(reporte, "reporte");
        Validar.obligatorio(destino, "archivo de destino");
        int columnas = reporte.columnas().size();
        Document documento = new Document(columnas >= COLUMNAS_PARA_HOJA_HORIZONTAL ? PageSize.A4.rotate() : PageSize.A4,
                36, 36, 36, 36);
        try (OutputStream salida = Files.newOutputStream(destino)) {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            documento.add(new Paragraph(reporte.titulo(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            documento.add(new Paragraph("Generado el " + FECHA.format(LocalDateTime.now()),
                    FontFactory.getFont(FontFactory.HELVETICA, 9)));
            documento.add(new Paragraph(" "));
            documento.add(construirTabla(reporte));
            if (reporte.estaVacio()) {
                documento.add(new Paragraph("No hay datos para los filtros elegidos."));
            }
            documento.close();
        } catch (IOException | DocumentException e) {
            throw new ExportacionException("No se pudo crear el PDF en " + destino + ".", e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
    }

    public static void aExcel(Reporte reporte, Path destino) {
        Validar.obligatorio(reporte, "reporte");
        Validar.obligatorio(destino, "archivo de destino");
        try (Workbook libro = new XSSFWorkbook(); OutputStream salida = Files.newOutputStream(destino)) {
            Sheet hoja = libro.createSheet(nombreDeHoja(reporte.titulo()));
            CellStyle negrita = libro.createCellStyle();
            org.apache.poi.ss.usermodel.Font fuente = libro.createFont();
            fuente.setBold(true);
            negrita.setFont(fuente);

            Row encabezado = hoja.createRow(0);
            for (int c = 0; c < reporte.columnas().size(); c++) {
                Cell celda = encabezado.createCell(c);
                celda.setCellValue(reporte.columnas().get(c));
                celda.setCellStyle(negrita);
            }
            for (int f = 0; f < reporte.filas().size(); f++) {
                Row fila = hoja.createRow(f + 1);
                List<Object> valores = reporte.filas().get(f);
                for (int c = 0; c < valores.size(); c++) {
                    escribirCelda(fila.createCell(c), valores.get(c));
                }
            }
            for (int c = 0; c < reporte.columnas().size(); c++) {
                hoja.autoSizeColumn(c);
            }
            libro.write(salida);
        } catch (IOException e) {
            throw new ExportacionException("No se pudo crear el archivo de Excel en " + destino + ".", e);
        }
    }

    private static PdfPTable construirTabla(Reporte reporte) {
        PdfPTable tabla = new PdfPTable(reporte.columnas().size());
        tabla.setWidthPercentage(100);
        Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (String columna : reporte.columnas()) {
            PdfPCell celda = new PdfPCell(new Phrase(columna, negrita));
            celda.setBackgroundColor(new java.awt.Color(230, 230, 230));
            tabla.addCell(celda);
        }
        for (List<Object> fila : reporte.filas()) {
            for (Object valor : fila) {
                PdfPCell celda = new PdfPCell(new Phrase(comoTexto(valor), normal));
                if (valor instanceof Number) {
                    celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                }
                tabla.addCell(celda);
            }
        }
        tabla.setHeaderRows(1);
        return tabla;
    }

    private static void escribirCelda(Cell celda, Object valor) {
        if (valor instanceof BigDecimal numero) {
            celda.setCellValue(numero.doubleValue());
        } else if (valor instanceof Number numero) {
            celda.setCellValue(numero.doubleValue());
        } else {
            celda.setCellValue(valor == null ? "" : comoTexto(valor));
        }
    }

    private static String comoTexto(Object valor) {
        if (valor == null) {
            return "-";
        }
        if (valor instanceof LocalDateTime fecha) {
            return FECHA.format(fecha);
        }
        if (valor instanceof BigDecimal numero) {
            return numero.toPlainString();
        }
        return valor.toString();
    }

    // Excel no admite en el nombre de hoja los caracteres : \ / ? * [ ] ni más de 31 letras.
    private static String nombreDeHoja(String titulo) {
        String limpio = titulo.replaceAll("[:\\\\/?*\\[\\]]", " ");
        return limpio.length() > LARGO_MAXIMO_NOMBRE_HOJA ? limpio.substring(0, LARGO_MAXIMO_NOMBRE_HOJA) : limpio;
    }
}
