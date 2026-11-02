package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.jdbc.AsignacionDaoJdbc;
import com.taller.nucleo.dao.jdbc.CompraDaoJdbc;
import com.taller.nucleo.dao.jdbc.ConsumoRepuestoDaoJdbc;
import com.taller.nucleo.dao.jdbc.DiagnosticoDaoJdbc;
import com.taller.nucleo.dao.jdbc.ObservacionOrdenDaoJdbc;
import com.taller.nucleo.dao.jdbc.OrdenServicioDaoJdbc;
import com.taller.nucleo.dao.jdbc.PagoDaoJdbc;
import com.taller.nucleo.dao.jdbc.PresupuestoDaoJdbc;
import com.taller.nucleo.dao.jdbc.RepuestoDaoJdbc;
import com.taller.nucleo.dao.jdbc.TecnicoDaoJdbc;
import com.taller.nucleo.dao.jdbc.UsuarioDaoJdbc;
import com.taller.nucleo.dao.jdbc.VehiculoDaoJdbc;
import com.taller.nucleo.entidad.Asignacion;
import com.taller.nucleo.entidad.Compra;
import com.taller.nucleo.entidad.ConsumoRepuesto;
import com.taller.nucleo.entidad.Diagnostico;
import com.taller.nucleo.entidad.ObservacionOrden;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Pago;
import com.taller.nucleo.entidad.Presupuesto;
import com.taller.nucleo.entidad.Repuesto;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.entidad.Vehiculo;
import com.taller.nucleo.seguridad.Accion;
import com.taller.nucleo.seguridad.Permisos;
import com.taller.nucleo.util.Transaccion;

import java.util.List;
import java.util.Optional;

/**
 * Consultas de solo lectura que necesitan las pantallas (listas y detalle de una orden).
 * Existen para que la interfaz nunca hable directo con los DAO. No modifican datos.
 */
public final class ConsultaServicio {

    public List<OrdenServicio> ordenes(String estado, String texto) {
        return Transaccion.devolviendo(c -> new OrdenServicioDaoJdbc(c).buscar(estado, texto));
    }

    public Optional<OrdenServicio> orden(int idOrden) {
        return Transaccion.devolviendo(c -> new OrdenServicioDaoJdbc(c).buscarPorId(idOrden));
    }

    /** Para avisar antes de crear una orden si la regla 1 pedirá la autorización de un administrador. */
    public boolean existeOrdenActiva(int idVehiculo, int idTipoAtencion) {
        return Transaccion.devolviendo(c -> new OrdenServicioDaoJdbc(c).existeActiva(idVehiculo, idTipoAtencion));
    }

    public List<Vehiculo> vehiculos(String texto) {
        return Transaccion.devolviendo(c -> new VehiculoDaoJdbc(c).buscar(texto));
    }

    public List<Presupuesto> presupuestos(int idOrden) {
        return Transaccion.devolviendo(c -> new PresupuestoDaoJdbc(c).listarPorOrden(idOrden));
    }

    public List<Diagnostico> diagnosticos(int idOrden) {
        return Transaccion.devolviendo(c -> new DiagnosticoDaoJdbc(c).listarPorOrden(idOrden));
    }

    public List<Asignacion> asignaciones(int idOrden) {
        return Transaccion.devolviendo(c -> new AsignacionDaoJdbc(c).listarPorOrden(idOrden));
    }

    public List<Pago> pagos(int idOrden) {
        return Transaccion.devolviendo(c -> new PagoDaoJdbc(c).listarPorOrden(idOrden));
    }

    public List<ConsumoRepuesto> consumos(int idOrden) {
        return Transaccion.devolviendo(c -> new ConsumoRepuestoDaoJdbc(c).listarPorOrden(idOrden));
    }

    public List<ObservacionOrden> observaciones(int idOrden) {
        return Transaccion.devolviendo(c -> new ObservacionOrdenDaoJdbc(c).listarPorOrden(idOrden));
    }

    public List<Tecnico> tecnicosActivos() {
        return Transaccion.devolviendo(c -> new TecnicoDaoJdbc(c).listarActivos());
    }

    public List<Repuesto> repuestos(String texto) {
        return Transaccion.devolviendo(c -> new RepuestoDaoJdbc(c).buscar(texto));
    }

    public List<Repuesto> repuestosConStockBajo() {
        return Transaccion.devolviendo(c -> new RepuestoDaoJdbc(c).listarStockBajo());
    }

    public List<Compra> compras(Usuario solicitante) {
        Permisos.exigir(solicitante, Accion.GESTIONAR_INVENTARIO);
        return Transaccion.devolviendo(c -> new CompraDaoJdbc(c).listar());
    }

    public List<Usuario> usuarios(Usuario administrador) {
        Permisos.exigir(administrador, Accion.GESTIONAR_USUARIOS);
        return Transaccion.devolviendo(c -> new UsuarioDaoJdbc(c).listar());
    }
}
