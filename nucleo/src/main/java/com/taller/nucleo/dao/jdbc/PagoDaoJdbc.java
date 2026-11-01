package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.PagoDao;
import com.taller.nucleo.entidad.MetodoPago;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Pago;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PagoDaoJdbc implements PagoDao {

    private static final String SELECCION = "SELECT id, id_orden, id_usuario, monto, metodo, fecha FROM pago ";

    private final Connection conexion;
    private final OrdenServicioDaoJdbc ordenes;
    private final UsuarioDaoJdbc usuarios;

    public PagoDaoJdbc(Connection conexion) {
        this.conexion = conexion;
        this.ordenes = new OrdenServicioDaoJdbc(conexion);
        this.usuarios = new UsuarioDaoJdbc(conexion);
    }

    private Pago mapear(ResultSet fila, Map<Integer, OrdenServicio> cache) throws SQLException {
        Pago pago = new Pago(ordenes.cargar(fila.getInt("id_orden"), cache),
                Jdbc.requerido(usuarios.buscarPorId(fila.getInt("id_usuario")), "el usuario del pago"),
                fila.getBigDecimal("monto"), MetodoPago.valueOf(fila.getString("metodo")));
        pago.setFecha(Jdbc.fecha(fila, "fecha"));
        pago.setId(fila.getInt("id"));
        return pago;
    }

    @Override
    public Pago insertar(Pago pago) {
        pago.setId(Jdbc.insertar(conexion,
                "INSERT INTO pago (id_orden, id_usuario, monto, metodo, fecha) VALUES (?, ?, ?, ?, ?)",
                pago.getOrden().getId(), pago.getUsuario().getId(), pago.getMonto(), pago.getMetodo(),
                pago.getFecha()));
        return pago;
    }

    @Override
    public Optional<Pago> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE id = ?", fila -> mapear(fila, new HashMap<>()), id);
    }

    @Override
    public List<Pago> listar() {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY fecha DESC, id DESC", fila -> mapear(fila, cache));
    }

    @Override
    public List<Pago> listarPorOrden(int idOrden) {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "WHERE id_orden = ? ORDER BY fecha, id",
                fila -> mapear(fila, cache), idOrden);
    }

    @Override
    public BigDecimal totalPagado(int idOrden) {
        return Jdbc.uno(conexion, "SELECT COALESCE(SUM(monto), 0) FROM pago WHERE id_orden = ?",
                fila -> fila.getBigDecimal(1), idOrden).orElse(BigDecimal.ZERO);
    }
}
