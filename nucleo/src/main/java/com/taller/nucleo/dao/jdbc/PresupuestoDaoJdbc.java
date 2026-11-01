package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.PresupuestoDao;
import com.taller.nucleo.entidad.EstadoPresupuesto;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Presupuesto;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.PersistenciaException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PresupuestoDaoJdbc implements PresupuestoDao {

    private static final String SELECCION =
            "SELECT id, id_orden, estado, fecha, id_usuario_aprobador FROM presupuesto ";

    private final Connection conexion;
    private final OrdenServicioDaoJdbc ordenes;
    private final UsuarioDaoJdbc usuarios;

    public PresupuestoDaoJdbc(Connection conexion) {
        this.conexion = conexion;
        this.ordenes = new OrdenServicioDaoJdbc(conexion);
        this.usuarios = new UsuarioDaoJdbc(conexion);
    }

    private Presupuesto mapear(ResultSet fila, Map<Integer, OrdenServicio> cache) throws SQLException {
        Presupuesto presupuesto = new Presupuesto(ordenes.cargar(fila.getInt("id_orden"), cache));
        Integer idAprobador = Jdbc.enteroOpcional(fila, "id_usuario_aprobador");
        Usuario aprobador = idAprobador == null ? null
                : Jdbc.requerido(usuarios.buscarPorId(idAprobador), "el usuario que resolvió el presupuesto");
        presupuesto.restaurar(EstadoPresupuesto.valueOf(fila.getString("estado")),
                Jdbc.fecha(fila, "fecha"), aprobador);
        presupuesto.setId(fila.getInt("id"));
        cargarDetalles(presupuesto);
        return presupuesto;
    }

    private void cargarDetalles(Presupuesto presupuesto) {
        Jdbc.consultar(conexion,
                "SELECT s.id, s.nombre, s.precio_base, d.cantidad_horas, d.precio_unitario AS precio_detalle "
                        + "FROM detalle_servicio d JOIN servicio s ON s.id = d.id_servicio "
                        + "WHERE d.id_presupuesto = ? ORDER BY d.id",
                fila -> new Presupuesto.DetalleServicio(ServicioDaoJdbc.mapear(fila),
                        fila.getBigDecimal("cantidad_horas"), fila.getBigDecimal("precio_detalle")),
                presupuesto.getId()).forEach(presupuesto::restaurarDetalle);
        Jdbc.consultar(conexion,
                "SELECT r.id, r.codigo, r.nombre, r.stock, r.stock_minimo, r.precio_costo, r.precio_venta, "
                        + "d.cantidad, d.precio_unitario AS precio_detalle "
                        + "FROM detalle_presupuesto_repuesto d JOIN repuesto r ON r.id = d.id_repuesto "
                        + "WHERE d.id_presupuesto = ? ORDER BY d.id",
                fila -> new Presupuesto.DetalleRepuesto(RepuestoDaoJdbc.mapear(fila),
                        fila.getInt("cantidad"), fila.getBigDecimal("precio_detalle")),
                presupuesto.getId()).forEach(presupuesto::restaurarDetalle);
    }

    @Override
    public Presupuesto insertar(Presupuesto presupuesto) {
        if (presupuesto.getOrden().getId() == null) {
            throw new PersistenciaException("La orden del presupuesto debe estar guardada antes.");
        }
        int id = Jdbc.insertar(conexion,
                "INSERT INTO presupuesto (id_orden, estado, fecha, total, id_usuario_aprobador) "
                        + "VALUES (?, ?, ?, ?, ?)",
                presupuesto.getOrden().getId(), presupuesto.getEstado(), presupuesto.getFecha(),
                presupuesto.total(),
                presupuesto.getAprobador() == null ? null : presupuesto.getAprobador().getId());
        presupuesto.setId(id);
        for (Presupuesto.DetalleServicio detalle : presupuesto.getServicios()) {
            Jdbc.insertar(conexion,
                    "INSERT INTO detalle_servicio (id_presupuesto, id_servicio, cantidad_horas, precio_unitario) "
                            + "VALUES (?, ?, ?, ?)",
                    id, detalle.servicio().getId(), detalle.horas(), detalle.precioUnitario());
        }
        for (Presupuesto.DetalleRepuesto detalle : presupuesto.getRepuestos()) {
            Jdbc.insertar(conexion,
                    "INSERT INTO detalle_presupuesto_repuesto (id_presupuesto, id_repuesto, cantidad, precio_unitario) "
                            + "VALUES (?, ?, ?, ?)",
                    id, detalle.repuesto().getId(), detalle.cantidad(), detalle.precioUnitario());
        }
        return presupuesto;
    }

    @Override
    public void actualizarEstado(Presupuesto presupuesto) {
        Jdbc.ejecutarExigiendoFila(conexion,
                "UPDATE presupuesto SET estado = ?, id_usuario_aprobador = ? WHERE id = ?",
                presupuesto.getEstado(),
                presupuesto.getAprobador() == null ? null : presupuesto.getAprobador().getId(),
                presupuesto.getId());
    }

    @Override
    public Optional<Presupuesto> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE id = ?", fila -> mapear(fila, new HashMap<>()), id);
    }

    @Override
    public List<Presupuesto> listar() {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY fecha DESC, id DESC", fila -> mapear(fila, cache));
    }

    @Override
    public List<Presupuesto> listarPorOrden(int idOrden) {
        Map<Integer, OrdenServicio> cache = new HashMap<>();
        return Jdbc.consultar(conexion, SELECCION + "WHERE id_orden = ? ORDER BY fecha, id",
                fila -> mapear(fila, cache), idOrden);
    }

    @Override
    public Optional<Presupuesto> buscarAprobadoDeOrden(int idOrden) {
        return Jdbc.uno(conexion,
                SELECCION + "WHERE id_orden = ? AND estado = 'APROBADO' ORDER BY id DESC LIMIT 1",
                fila -> mapear(fila, new HashMap<>()), idOrden);
    }
}
