package com.taller.nucleo.dao.jdbc;

import com.taller.nucleo.dao.OrdenServicioDao;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Prioridad;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.estado.EstadoOrden;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class OrdenServicioDaoJdbc implements OrdenServicioDao {

    private static final String SELECCION = "SELECT o.id, o.id_vehiculo, o.id_tipo_atencion, o.prioridad, "
            + "o.estado, o.fecha_ingreso, o.fecha_entrega, o.id_usuario_registro, "
            + "o.id_usuario_autoriza_duplicada, o.id_usuario_autoriza_credito "
            + "FROM orden_servicio o JOIN vehiculo v ON v.id = o.id_vehiculo "
            + "JOIN cliente c ON c.id = v.id_cliente ";

    private final Connection conexion;
    private final VehiculoDaoJdbc vehiculos;
    private final TipoAtencionDaoJdbc tipos;
    private final UsuarioDaoJdbc usuarios;

    public OrdenServicioDaoJdbc(Connection conexion) {
        this.conexion = conexion;
        this.vehiculos = new VehiculoDaoJdbc(conexion);
        this.tipos = new TipoAtencionDaoJdbc(conexion);
        this.usuarios = new UsuarioDaoJdbc(conexion);
    }

    private OrdenServicio mapear(ResultSet fila) throws SQLException {
        OrdenServicio orden = new OrdenServicio(
                Jdbc.requerido(vehiculos.buscarPorId(fila.getInt("id_vehiculo")), "el vehículo de la orden"),
                Jdbc.requerido(tipos.buscarPorId(fila.getInt("id_tipo_atencion")), "el tipo de atención"),
                Prioridad.valueOf(fila.getString("prioridad")),
                Jdbc.requerido(usuarios.buscarPorId(fila.getInt("id_usuario_registro")), "el usuario que registró"));
        orden.restaurar(EstadoOrden.desde(fila.getString("estado")), Jdbc.fecha(fila, "fecha_ingreso"),
                Jdbc.fecha(fila, "fecha_entrega"),
                usuarioOpcional(Jdbc.enteroOpcional(fila, "id_usuario_autoriza_duplicada")),
                usuarioOpcional(Jdbc.enteroOpcional(fila, "id_usuario_autoriza_credito")));
        orden.setId(fila.getInt("id"));
        return orden;
    }

    private Usuario usuarioOpcional(Integer id) {
        return id == null ? null : Jdbc.requerido(usuarios.buscarPorId(id), "el usuario que autorizó");
    }

    private static Integer idDe(Usuario usuario) {
        return usuario == null ? null : usuario.getId();
    }

    /** Usado por los DAO de detalle para no releer la misma orden por cada fila. */
    OrdenServicio cargar(int id, Map<Integer, OrdenServicio> cache) {
        OrdenServicio orden = cache.get(id);
        if (orden == null) {
            orden = Jdbc.requerido(buscarPorId(id), "la orden #" + id);
            cache.put(id, orden);
        }
        return orden;
    }

    @Override
    public OrdenServicio insertar(OrdenServicio orden) {
        orden.setId(Jdbc.insertar(conexion,
                "INSERT INTO orden_servicio (id_vehiculo, id_tipo_atencion, prioridad, estado, fecha_ingreso, "
                        + "fecha_entrega, credito_autorizado, id_usuario_registro, "
                        + "id_usuario_autoriza_duplicada, id_usuario_autoriza_credito) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                orden.getVehiculo().getId(), orden.getTipoAtencion().getId(), orden.getPrioridad(),
                orden.getEstado().getNombre(), orden.getFechaIngreso(), orden.getFechaEntrega(),
                orden.tieneCreditoAutorizado(), orden.getUsuarioRegistro().getId(),
                idDe(orden.getAutorizaDuplicada()), idDe(orden.getAutorizaCredito())));
        return orden;
    }

    @Override
    public void actualizar(OrdenServicio orden) {
        Jdbc.ejecutarExigiendoFila(conexion,
                "UPDATE orden_servicio SET prioridad = ?, estado = ?, fecha_entrega = ?, credito_autorizado = ?, "
                        + "id_usuario_autoriza_duplicada = ?, id_usuario_autoriza_credito = ? WHERE id = ?",
                orden.getPrioridad(), orden.getEstado().getNombre(), orden.getFechaEntrega(),
                orden.tieneCreditoAutorizado(), idDe(orden.getAutorizaDuplicada()),
                idDe(orden.getAutorizaCredito()), orden.getId());
    }

    @Override
    public Optional<OrdenServicio> buscarPorId(int id) {
        return Jdbc.uno(conexion, SELECCION + "WHERE o.id = ?", this::mapear, id);
    }

    @Override
    public List<OrdenServicio> listar() {
        return Jdbc.consultar(conexion, SELECCION + "ORDER BY o.fecha_ingreso DESC, o.id DESC", this::mapear);
    }

    @Override
    public boolean existeActiva(int idVehiculo, int idTipoAtencion) {
        return Jdbc.uno(conexion,
                "SELECT COUNT(*) FROM orden_servicio WHERE id_vehiculo = ? AND id_tipo_atencion = ? "
                        + "AND estado NOT IN ('ENTREGADA', 'CANCELADA')",
                fila -> fila.getInt(1), idVehiculo, idTipoAtencion).orElse(0) > 0;
    }

    @Override
    public List<OrdenServicio> buscar(String estado, String texto) {
        String estadoFiltro = estado == null || estado.isBlank() ? null : estado.trim();
        String textoFiltro = texto == null || texto.isBlank() ? null : Jdbc.parcial(texto);
        return Jdbc.consultar(conexion,
                SELECCION + "WHERE (? IS NULL OR o.estado = ?) "
                        + "AND (? IS NULL OR v.placa LIKE ? OR c.nombre LIKE ?) "
                        + "ORDER BY o.fecha_ingreso DESC, o.id DESC",
                this::mapear, estadoFiltro, estadoFiltro, textoFiltro, textoFiltro, textoFiltro);
    }

    @Override
    public List<OrdenServicio> listarPorVehiculo(int idVehiculo) {
        return Jdbc.consultar(conexion, SELECCION + "WHERE o.id_vehiculo = ? ORDER BY o.fecha_ingreso DESC, o.id DESC",
                this::mapear, idVehiculo);
    }
}
