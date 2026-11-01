package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.RepuestoDao;
import com.taller.nucleo.entidad.Repuesto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class RepuestoDaoJdbc implements RepuestoDao {

    private static final String SELECCION =
            "SELECT id, codigo, nombre, stock, stock_minimo, precio_costo, precio_venta FROM repuesto ";

    private final Connection conexion;

    public RepuestoDaoJdbc(Connection conexion) {
        this.conexion = conexion;
    }

    static Repuesto mapear(ResultSet fila) throws SQLException {
        Repuesto repuesto = new Repuesto(fila.getString("codigo"), fila.getString("nombre"),
                fila.getBigDecimal("precio_costo"), fila.getBigDecimal("precio_venta"));
        repuesto.setStock(fila.getInt("stock"));
        repuesto.setStockMinimo(fila.getInt("stock_minimo"));
        repuesto.setId(fila.getInt("id"));
        return repuesto;
    }

    @Override
    public Repuesto insertar(Repuesto repuesto) {
        repuesto.setId(Jdbc.insertar(conexion,
                "INSERT INTO repuesto (codigo, nombre, stock, stock_minimo, precio_costo, precio_venta) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                repuesto.getCodigo(), repuesto.getNombre(), repuesto.getStock(), repuesto.getStockMinimo(),
                repuesto.getPrecioCosto(), repuesto.getPrecioVenta()));
        return repuesto;
    }

    @Override
    public Optional<Repuesto> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE id = ?", RepuestoDaoJdbc::mapear, id);
    }

    @Override
    public List<Repuesto> listar() {
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY nombre", RepuestoDaoJdbc::mapear);
    }

    @Override
    public List<Repuesto> buscar(String texto) {
        String patron = Jdbc.parcial(texto);
        return Jdbc.consultar(conexion, SELECCION + "WHERE codigo LIKE ? OR nombre LIKE ? ORDER BY nombre",
                RepuestoDaoJdbc::mapear, patron, patron);
    }

    @Override
    public List<Repuesto> listarStockBajo() {
        return Jdbc.consultar(conexion, SELECCION + "WHERE stock <= stock_minimo ORDER BY nombre",
                RepuestoDaoJdbc::mapear);
    }

    @Override
    public boolean ajustarStock(int idRepuesto, int variacion) {
        return Jdbc.ejecutar(conexion, "UPDATE repuesto SET stock = stock + ? WHERE id = ? AND stock + ? >= 0",
                variacion, idRepuesto, variacion) == 1;
    }

    @Override
    public void actualizar(Repuesto repuesto) {
        Jdbc.ejecutarExigiendoFila(conexion,
                "UPDATE repuesto SET codigo = ?, nombre = ?, stock = ?, stock_minimo = ?, precio_costo = ?, "
                        + "precio_venta = ? WHERE id = ?",
                repuesto.getCodigo(), repuesto.getNombre(), repuesto.getStock(), repuesto.getStockMinimo(),
                repuesto.getPrecioCosto(), repuesto.getPrecioVenta(), repuesto.getId());
    }

    @Override
    public void eliminar(int id) {
        Jdbc.ejecutarExigiendoFila(conexion, "DELETE FROM repuesto WHERE id = ?", id);
    }
}
