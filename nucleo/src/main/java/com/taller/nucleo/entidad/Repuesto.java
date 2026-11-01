package com.taller.nucleo.entidad;

import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.util.Validar;

import java.math.BigDecimal;

public final class Repuesto {

    private Integer id;
    private String codigo;
    private String nombre;
    private int stock;
    private int stockMinimo;
    private BigDecimal precioCosto;
    private BigDecimal precioVenta;

    public Repuesto(String codigo, String nombre, BigDecimal precioCosto, BigDecimal precioVenta) {
        setCodigo(codigo);
        setNombre(nombre);
        setPrecioCosto(precioCosto);
        setPrecioVenta(precioVenta);
    }

    public boolean hayStock(int cantidad) {
        return stock >= cantidad;
    }

    public boolean stockBajo() {
        return stock <= stockMinimo;
    }

    /** Descuenta del stock en memoria; el servicio lo persiste dentro de la transacción. */
    public void descontar(int cantidad) {
        Validar.enteroPositivo(cantidad, "cantidad");
        if (!hayStock(cantidad)) {
            throw new ReglaNegocioException("Stock insuficiente de " + nombre
                    + ": hay " + stock + " y se piden " + cantidad + ".");
        }
        stock -= cantidad;
    }

    public void aumentar(int cantidad) {
        stock += Validar.enteroPositivo(cantidad, "cantidad");
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = Validar.textoObligatorio(codigo, "código", 30);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = Validar.textoObligatorio(nombre, "nombre del repuesto", 100);
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = Validar.enteroMinimo(stock, 0, "stock");
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = Validar.enteroMinimo(stockMinimo, 0, "stock mínimo");
    }

    public BigDecimal getPrecioCosto() {
        return precioCosto;
    }

    public void setPrecioCosto(BigDecimal precioCosto) {
        this.precioCosto = Validar.montoNoNegativo(precioCosto, "precio de costo");
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = Validar.montoNoNegativo(precioVenta, "precio de venta");
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
