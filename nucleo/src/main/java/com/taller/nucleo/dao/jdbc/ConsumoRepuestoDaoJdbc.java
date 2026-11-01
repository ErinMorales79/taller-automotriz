package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.ConsumoRepuestoDao;
import com.taller.nucleo.entidad.ConsumoRepuesto;
import com.taller.nucleo.entidad.OrdenServicio;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ConsumoRepuestoDaoJdbc implements ConsumoRepuestoDao {

    private static final String SELECCION = "SELECT id, id_orden, id_repuesto, id_usuario, cantidad, "
            + "precio_unitario, fecha FROM consumo_repuesto ";

    private final Connection conexion;
    private final OrdenServicioDaoJdbc ordenes;
    private final RepuestoDaoJdbc repuestos;
    private final UsuarioDaoJdbc usuarios;

    public ConsumoRepuestoDaoJdbc(Connection conexion) {
        this.conexion = conexion;
        this.ordenes = new OrdenServicioDaoJdbc(conexion);
        this.repuestos = new RepuestoDaoJdbc(conexion);
        this.usuarios = new UsuarioDaoJdbc(conexion);
    }

    private ConsumoRepuesto mapear(ResultSet fila, Map<Integer, OrdenServicio> cache) throws SQLException {
        ConsumoRepuesto consumo = new ConsumoRepuesto(
                ordenes.cargar(fila.getInt("id_orden"), cache),
                Jdbc.requerido(repuestos.buscarPorId(fila.getInt("id_repuesto")), "el repuesto consumido"),
                Jdbc.requerido(usuarios.buscarPorId(fila.getInt("id_usuario")), "el usuario del consumo"),
                fila.getInt("cantidad"), fila.getBigDecimal("precio_unitario"));
        consumo.setFecha(Jdbc.fecha(fila, "fecha"));
        consumo.setId(fila.getInt("id"));
        return consumo;
    }

    @Override
    public ConsumoRepuesto insertar(ConsumoRepuesto consumo) {
        consumo.setId(Jdbc.insertar(conexion,
                "INSERT INTO consumo_repuesto (id_orden, id_repuesto, id_usuario, cantidad, precio_unitario, fecha) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                consumo.getOrden().getId(), consumo.getRepuesto().getId(), consumo.getUsuario().getId(),
                consumo.getCantidad(), consumo.getPrecioUnitario(), consumo.getFecha()));
        return consumo;
    }

    @Override
    public Optional<ConsumoRepuesto> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE id = ?", fila -> mapear(fila, new HashMap<>()), id);
    }

    @Override
    public List<ConsumoRepuesto> listar() {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY fecha DESC, id DESC", fila -> mapear(fila, cache));
    }

    @Override
    public List<ConsumoRepuesto> listarPorOrden(int idOrden) {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "WHERE id_orden = ? ORDER BY fecha, id",
                fila -> mapear(fila, cache), idOrden);
    }
}
