package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.jdbc.AuditoriaDaoJdbc;
import com.taller.nucleo.entidad.Auditoria;
import com.taller.nucleo.entidad.Cliente;
import com.taller.nucleo.entidad.Repuesto;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Servicio;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.TipoAtencion;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.AccesoDenegadoException;
import com.taller.nucleo.excepcion.PersistenciaException;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.soporte.BaseBD;
import com.taller.nucleo.soporte.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** CRUD completo con permisos por rol y auditoría, más cambio de rol de usuarios. */
class CatalogosTest extends BaseBD {

    private Datos datos;
    private Usuario admin;
    private Usuario recepcion;
    private Usuario encargado;

    @BeforeEach
    void preparar() {
        datos = new Datos(conexion());
        admin = datos.usuario(Rol.ADMINISTRADOR);
        recepcion = datos.usuario(Rol.RECEPCIONISTA);
        encargado = datos.usuario(Rol.ENCARGADO_INVENTARIO);
    }

    @Test
    void recepcionCreaEditaBuscaYEliminaClientes() {
        var clientes = Catalogos.clientes();
        Cliente cliente = clientes.crear(recepcion, new Cliente(unico("D"), "Ana Servicio"));
        assertNotNull(cliente.getId());

        cliente.setNombre("Ana Editada");
        clientes.actualizar(recepcion, cliente);
        assertEquals("Ana Editada", clientes.buscarPorId(cliente.getId()).orElseThrow().getNombre());
        assertEquals(1, clientes.consultar(dao -> dao.buscar(cliente.getDocumento())).size());

        clientes.eliminar(recepcion, cliente.getId());
        assertTrue(clientes.buscarPorId(cliente.getId()).isEmpty());
    }

    @Test
    void cadaOperacionQuedaAuditada() {
        var clientes = Catalogos.clientes();
        Cliente cliente = clientes.crear(recepcion, new Cliente(unico("D"), "Auditado"));
        clientes.actualizar(recepcion, cliente);
        clientes.eliminar(recepcion, cliente.getId());

        List<String> acciones = new AuditoriaDaoJdbc(conexion()).listarPorUsuario(recepcion.getId()).stream()
                .map(Auditoria::getAccion).toList();
        assertEquals(List.of("ELIMINAR", "ACTUALIZAR", "CREAR"), acciones);
    }

    @Test
    void sinPermisoNoSeEscribeNada() {
        Cliente cliente = new Cliente(unico("D"), "Sin permiso");
        assertThrows(AccesoDenegadoException.class, () -> Catalogos.clientes().crear(encargado, cliente));
        assertTrue(Catalogos.clientes().consultar(dao -> dao.buscarPorDocumento(cliente.getDocumento())).isEmpty());

        assertThrows(AccesoDenegadoException.class,
                () -> Catalogos.repuestos().crear(recepcion, new Repuesto(unico("R"), "X", BigDecimal.ONE, BigDecimal.TEN)));
        assertThrows(AccesoDenegadoException.class,
                () -> Catalogos.tiposAtencion().crear(recepcion, new TipoAtencion(unico("T"))));
    }

    @Test
    void encargadoGestionaRepuestosYAdministradorLosCatalogos() {
        Repuesto repuesto = Catalogos.repuestos().crear(encargado,
                new Repuesto(unico("R"), "Filtro", BigDecimal.ONE, BigDecimal.TEN));
        repuesto.setStockMinimo(4);
        Catalogos.repuestos().actualizar(encargado, repuesto);
        assertEquals(4, Catalogos.repuestos().buscarPorId(repuesto.getId()).orElseThrow().getStockMinimo());

        assertNotNull(Catalogos.tiposAtencion().crear(admin, new TipoAtencion(unico("T"))).getId());
        assertNotNull(Catalogos.servicios().crear(admin, new Servicio(unico("S"), BigDecimal.TEN)).getId());
        assertNotNull(Catalogos.tecnicos().crear(admin, new Tecnico(unico("Tec"))).getId());
    }

    @Test
    void editarUnRegistroSinGuardarOEliminarUnoConDependenciasFalla() {
        assertThrows(ReglaNegocioException.class,
                () -> Catalogos.clientes().actualizar(recepcion, new Cliente(unico("D"), "Nuevo")));

        var vehiculo = datos.vehiculo();
        assertThrows(PersistenciaException.class,
                () -> Catalogos.clientes().eliminar(recepcion, vehiculo.getPropietario().getId()));
        assertTrue(Catalogos.clientes().buscarPorId(vehiculo.getPropietario().getId()).isPresent(),
                "La transacción fallida no deja nada a medias");
    }

    @Test
    void cambiarRolQuedaAuditadoYUnCambioFallidoDejaAlUsuarioIgual() {
        UsuarioServicio usuarios = new UsuarioServicio();
        usuarios.cambiarRol(admin, encargado.getId(), Rol.RECEPCIONISTA, null);
        assertTrue(new AuditoriaDaoJdbc(conexion()).listarPorUsuario(admin.getId()).stream()
                .anyMatch(a -> a.getAccion().equals("CAMBIAR_ROL") && encargado.getId().equals(a.getIdRegistro())));

        assertThrows(ReglaNegocioException.class,
                () -> usuarios.cambiarRol(admin, encargado.getId(), Rol.TECNICO, null));
        var guardado = new com.taller.nucleo.dao.jdbc.UsuarioDaoJdbc(conexion()).buscarPorId(encargado.getId()).orElseThrow();
        assertEquals(Rol.RECEPCIONISTA, guardado.getRol());
        assertEquals(null, guardado.getTecnico());
        assertEquals(1, new AuditoriaDaoJdbc(conexion()).listarPorUsuario(admin.getId()).stream()
                .filter(a -> a.getAccion().equals("CAMBIAR_ROL")).count(), "El intento fallido no se audita");
    }

    @Test
    void elAdministradorCambiaElRolDeOtroUsuarioPeroNoElSuyo() {
        UsuarioServicio usuarios = new UsuarioServicio();
        Tecnico tecnico = datos.tecnico();

        Usuario cambiado = usuarios.cambiarRol(admin, recepcion.getId(), Rol.TECNICO, tecnico);
        assertEquals(Rol.TECNICO, cambiado.getRol());
        assertEquals(tecnico.getId(), cambiado.getTecnico().getId());

        Usuario deVuelta = usuarios.cambiarRol(admin, recepcion.getId(), Rol.RECEPCIONISTA, null);
        assertEquals(Rol.RECEPCIONISTA, deVuelta.getRol());
        assertEquals(null, deVuelta.getTecnico());

        assertThrows(ReglaNegocioException.class, () -> usuarios.cambiarRol(admin, admin.getId(), Rol.TECNICO, tecnico));
        assertThrows(ReglaNegocioException.class, () -> usuarios.cambiarRol(admin, encargado.getId(), Rol.TECNICO, null));
        assertThrows(AccesoDenegadoException.class,
                () -> usuarios.cambiarRol(recepcion, encargado.getId(), Rol.ADMINISTRADOR, null));
    }
}
