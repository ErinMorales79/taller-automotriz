package com.taller.nucleo.dao;

import com.taller.nucleo.dao.jdbc.CompraDaoJdbc;
import com.taller.nucleo.dao.jdbc.PresupuestoDaoJdbc;
import com.taller.nucleo.dao.jdbc.ProveedorDaoJdbc;
import com.taller.nucleo.entidad.Compra;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Presupuesto;
import com.taller.nucleo.entidad.Proveedor;
import com.taller.nucleo.entidad.Repuesto;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.PersistenciaException;
import com.taller.nucleo.soporte.BaseBD;
import com.taller.nucleo.soporte.Datos;
import com.taller.nucleo.util.Transaccion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Operaciones encabezado-detalle: si un detalle falla, no queda nada guardado a medias. */
class DaoTransaccionesTest extends BaseBD {

    /** Repuesto que nunca se guardó: su id no existe y la clave foránea lo rechaza. */
    private static Repuesto repuestoInexistente() {
        Repuesto fantasma = new Repuesto("X", "Fantasma", BigDecimal.ONE, BigDecimal.TEN);
        fantasma.setId(2_000_000_000);
        return fantasma;
    }

    @Test
    void presupuestoConDetalleInvalidoDejaTodoSinGuardar() {
        Datos datos = new Datos(conexion());
        Usuario recepcion = datos.usuario(Rol.RECEPCIONISTA);
        OrdenServicio orden = datos.orden(recepcion);
        Presupuesto presupuesto = new Presupuesto(orden);
        presupuesto.agregarServicio(datos.servicio("20.00"), BigDecimal.ONE);
        presupuesto.agregarRepuesto(repuestoInexistente(), 1);

        assertThrows(PersistenciaException.class, () -> Transaccion.ejecutar(
                conexion -> new PresupuestoDaoJdbc(conexion).insertar(presupuesto)));

        assertTrue(new PresupuestoDaoJdbc(conexion()).listarPorOrden(orden.getId()).isEmpty(),
                "El encabezado no debe quedar guardado");
    }

    @Test
    void presupuestoValidoSeConfirmaCompleto() {
        Datos datos = new Datos(conexion());
        OrdenServicio orden = datos.orden(datos.usuario(Rol.RECEPCIONISTA));
        Presupuesto presupuesto = new Presupuesto(orden);
        presupuesto.agregarServicio(datos.servicio("20.00"), BigDecimal.ONE);

        Transaccion.ejecutar(conexion -> new PresupuestoDaoJdbc(conexion).insertar(presupuesto));

        assertEquals(1, new PresupuestoDaoJdbc(conexion()).listarPorOrden(orden.getId()).size());
    }

    @Test
    void compraConDetalleInvalidoDejaTodoSinGuardar() {
        Datos datos = new Datos(conexion());
        Usuario admin = datos.usuario(Rol.ADMINISTRADOR);
        Proveedor proveedor = new ProveedorDaoJdbc(conexion()).insertar(new Proveedor(unico("Prov ")));
        Compra compra = new Compra(proveedor, admin);
        compra.agregarDetalle(datos.repuesto(0, "5.00"), 2, BigDecimal.ONE);
        compra.agregarDetalle(repuestoInexistente(), 1, BigDecimal.ONE);

        assertThrows(PersistenciaException.class, () -> Transaccion.ejecutar(
                conexion -> new CompraDaoJdbc(conexion).insertar(compra)));

        assertTrue(new CompraDaoJdbc(conexion()).listar().stream()
                        .noneMatch(c -> c.getProveedor().getId().equals(proveedor.getId())),
                "La compra no debe quedar guardada");
    }
}
