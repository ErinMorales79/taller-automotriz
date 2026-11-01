package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.DaoModificable;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.seguridad.Accion;
import com.taller.nucleo.seguridad.Permisos;
import com.taller.nucleo.util.Transaccion;
import com.taller.nucleo.util.Validar;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * CRUD completo (crear, leer, editar, eliminar) con permiso por rol y auditoría, para las
 * entidades de catálogo. Se obtiene desde Catalogos. Las consultas no piden permiso: basta
 * con haber iniciado sesión en la pantalla.
 */
public final class CrudServicio<T, D extends DaoModificable<T>> {

    private final Function<Connection, D> fabricaDao;
    private final Accion accion;
    private final String tabla;
    private final Function<T, Integer> idDe;

    CrudServicio(Function<Connection, D> fabricaDao, Accion accion, String tabla, Function<T, Integer> idDe) {
        this.fabricaDao = fabricaDao;
        this.accion = accion;
        this.tabla = tabla;
        this.idDe = idDe;
    }

    public T crear(Usuario usuario, T entidad) {
        Permisos.exigir(usuario, accion);
        Validar.obligatorio(entidad, "registro");
        return Transaccion.devolviendo(conexion -> {
            fabricaDao.apply(conexion).insertar(entidad);
            AuditoriaServicio.registrar(conexion, usuario, "CREAR", tabla, idDe.apply(entidad));
            return entidad;
        });
    }

    public T actualizar(Usuario usuario, T entidad) {
        Permisos.exigir(usuario, accion);
        Validar.obligatorio(entidad, "registro");
        int id = Consultas.idGuardado(idDe.apply(entidad), "el registro antes de editarlo");
        return Transaccion.devolviendo(conexion -> {
            fabricaDao.apply(conexion).actualizar(entidad);
            AuditoriaServicio.registrar(conexion, usuario, "ACTUALIZAR", tabla, id);
            return entidad;
        });
    }

    public void eliminar(Usuario usuario, int id) {
        Permisos.exigir(usuario, accion);
        Transaccion.ejecutar(conexion -> {
            fabricaDao.apply(conexion).eliminar(id);
            AuditoriaServicio.registrar(conexion, usuario, "ELIMINAR", tabla, id);
        });
    }

    public Optional<T> buscarPorId(int id) {
        return Transaccion.devolviendo(conexion -> fabricaDao.apply(conexion).buscarPorId(id));
    }

    public List<T> listar() {
        return Transaccion.devolviendo(conexion -> fabricaDao.apply(conexion).listar());
    }

    /** Para las búsquedas propias de cada DAO, por ejemplo {@code clientes.consultar(d -> d.buscar("ana"))}. */
    public <R> R consultar(Function<D, R> consulta) {
        return Transaccion.devolviendo(conexion -> consulta.apply(fabricaDao.apply(conexion)));
    }
}
