package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.CompraDao;
import com.taller.nucleo.entidad.Compra;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class CompraDaoJdbc implements CompraDao {

    private static final String SELECCION = "SELECT id, id_proveedor, id_usuario, fecha FROM compra ";

    private final Connection conexion;
    private final ProveedorDaoJdbc proveedores;
    private final UsuarioDaoJdbc usuarios;

    public CompraDaoJdbc(Connection conexion) {
        this.conexion = conexion;
        this.proveedores = new ProveedorDaoJdbc(conexion);
        this.usuarios = new UsuarioDaoJdbc(conexion);
    }

    private Compra mapear(ResultSet fila) throws SQLException {
        Compra compra = new Compra(
                Jdbc.requerido(proveedores.buscarPorId(fila.getInt("id_proveedor")), "el proveedor de la compra"),
                Jdbc.requerido(usuarios.buscarPorId(fila.getInt("id_usuario")), "el usuario de la compra"));
        compra.setFecha(Jdbc.fecha(fila, "fecha"));
        compra.setId(fila.getInt("id"));
        Jdbc.consultar(conexion,
                "SELECT r.id, r.codigo, r.nombre, r.stock, r.stock_minimo, r.precio_costo, r.precio_venta, "
                        + "d.cantidad, d.costo_unitario FROM detalle_compra d "
                        + "JOIN repuesto r ON r.id = d.id_repuesto WHERE d.id_compra = ? ORDER BY d.id",
                detalle -> new Compra.DetalleCompra(RepuestoDaoJdbc.mapear(detalle),
                        detalle.getInt("cantidad"), detalle.getBigDecimal("costo_unitario")),
                compra.getId()).forEach(d -> compra.agregarDetalle(d.repuesto(), d.cantidad(), d.costoUnitario()));
        return compra;
    }

    @Override
    public Compra insertar(Compra compra) {
        int id = Jdbc.insertar(conexion,
                "INSERT INTO compra (id_proveedor, id_usuario, fecha, total) VALUES (?, ?, ?, ?)",
                compra.getProveedor().getId(), compra.getUsuario().getId(), compra.getFecha(), compra.total());
        compra.setId(id);
        for (Compra.DetalleCompra detalle : compra.getDetalles()) {
            Jdbc.insertar(conexion,
                    "INSERT INTO detalle_compra (id_compra, id_repuesto, cantidad, costo_unitario) VALUES (?, ?, ?, ?)",
                    id, detalle.repuesto().getId(), detalle.cantidad(), detalle.costoUnitario());
        }
        return compra;
    }

    @Override
    public Optional<Compra> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE id = ?", this::mapear, id);
    }

    @Override
    public List<Compra> listar() {
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY fecha DESC, id DESC", this::mapear);
    }
}
