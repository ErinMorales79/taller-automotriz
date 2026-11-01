package com.taller.nucleo.dao;

import com.taller.nucleo.dao.jdbc.ClienteDaoJdbc;
import com.taller.nucleo.dao.jdbc.MarcaDaoJdbc;
import com.taller.nucleo.dao.jdbc.ModeloDaoJdbc;
import com.taller.nucleo.dao.jdbc.ProveedorDaoJdbc;
import com.taller.nucleo.dao.jdbc.RepuestoDaoJdbc;
import com.taller.nucleo.dao.jdbc.ServicioDaoJdbc;
import com.taller.nucleo.dao.jdbc.TecnicoDaoJdbc;
import com.taller.nucleo.dao.jdbc.TipoAtencionDaoJdbc;
import com.taller.nucleo.dao.jdbc.UsuarioDaoJdbc;
import com.taller.nucleo.dao.jdbc.VehiculoDaoJdbc;
import com.taller.nucleo.entidad.Cliente;
import com.taller.nucleo.entidad.Marca;
import com.taller.nucleo.entidad.Modelo;
import com.taller.nucleo.entidad.Proveedor;
import com.taller.nucleo.entidad.Repuesto;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Servicio;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.TipoAtencion;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.entidad.Vehiculo;
import com.taller.nucleo.excepcion.PersistenciaException;
import com.taller.nucleo.soporte.BaseBD;
import com.taller.nucleo.soporte.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DaoMaestrosTest extends BaseBD {

    private Datos datos;

    @BeforeEach
    void preparar() {
        datos = new Datos(conexion());
    }

    @Test
    void clienteSeGuardaBuscaActualizaYElimina() {
        ClienteDao dao = new ClienteDaoJdbc(conexion());
        String documento = unico("DOC");
        Cliente cliente = new Cliente(documento, "Ana Prueba");
        cliente.setTelefono("5551234");
        cliente.setCorreo("ana@correo.com");
        dao.insertar(cliente);

        Cliente leido = dao.buscarPorId(cliente.getId()).orElseThrow();
        assertEquals("Ana Prueba", leido.getNombre());
        assertEquals("ana@correo.com", leido.getCorreo());
        assertEquals(cliente.getId(), dao.buscarPorDocumento(documento).orElseThrow().getId());
        assertEquals(1, dao.buscar(documento).size());
        assertTrue(dao.buscar("zzz-no-existe-zzz").isEmpty());

        leido.setNombre("Ana Cambiada");
        dao.actualizar(leido);
        assertEquals("Ana Cambiada", dao.buscarPorId(cliente.getId()).orElseThrow().getNombre());

        dao.eliminar(cliente.getId());
        assertTrue(dao.buscarPorId(cliente.getId()).isEmpty());
    }

    @Test
    void documentoRepetidoEsRechazadoConMensajeClaro() {
        ClienteDao dao = new ClienteDaoJdbc(conexion());
        String documento = unico("DOC");
        dao.insertar(new Cliente(documento, "Uno"));
        PersistenciaException error = assertThrows(PersistenciaException.class,
                () -> dao.insertar(new Cliente(documento, "Dos")));
        assertTrue(error.getMessage().contains("único"));
    }

    @Test
    void actualizarUnRegistroInexistenteFalla() {
        Cliente fantasma = new Cliente("X", "Fantasma");
        fantasma.setId(2_000_000_000);
        assertThrows(PersistenciaException.class, () -> new ClienteDaoJdbc(conexion()).actualizar(fantasma));
    }

    @Test
    void eliminarUnClienteConVehiculosEsRechazado() {
        Vehiculo vehiculo = datos.vehiculo();
        ClienteDao dao = new ClienteDaoJdbc(conexion());
        PersistenciaException error = assertThrows(PersistenciaException.class,
                () -> dao.eliminar(vehiculo.getPropietario().getId()));
        assertTrue(error.getMessage().contains("dependen"));
    }

    @Test
    void marcasYModelosSeRelacionan() {
        MarcaDao marcas = new MarcaDaoJdbc(conexion());
        ModeloDao modelos = new ModeloDaoJdbc(conexion());
        Marca marca = marcas.insertar(new Marca(unico("M")));
        modelos.insertar(new Modelo(marca, "A"));
        modelos.insertar(new Modelo(marca, "B"));

        assertEquals(2, modelos.listarPorMarca(marca.getId()).size());
        assertEquals(marca.getNombre(), marcas.buscarPorNombre(marca.getNombre()).orElseThrow().getNombre());
        assertEquals(marca.getNombre(), modelos.listarPorMarca(marca.getId()).get(0).getMarca().getNombre());
    }

    @Test
    void vehiculoSeGuardaConSusRelacionesYSeBusca() {
        VehiculoDao dao = new VehiculoDaoJdbc(conexion());
        Cliente propietario = datos.cliente();
        Vehiculo vehiculo = new Vehiculo(unico("pl"), propietario, datos.modelo(), 2019);
        vehiculo.setColor("Rojo");
        vehiculo.setKilometraje(1500);
        dao.insertar(vehiculo);

        Vehiculo leido = dao.buscarPorPlaca(vehiculo.getPlaca().toLowerCase()).orElseThrow();
        assertEquals(propietario.getId(), leido.getPropietario().getId());
        assertEquals("Rojo", leido.getColor());
        assertEquals(1500, leido.getKilometraje());
        assertEquals(1, dao.listarPorCliente(propietario.getId()).size());
        assertEquals(1, dao.buscar(propietario.getNombre()).size());

        leido.setKilometraje(2000);
        dao.actualizar(leido);
        assertEquals(2000, dao.buscarPorId(leido.getId()).orElseThrow().getKilometraje());
    }

    @Test
    void usuarioTecnicoQuedaVinculadoYElRolSeConserva() {
        TecnicoDao tecnicos = new TecnicoDaoJdbc(conexion());
        UsuarioDao usuarios = new UsuarioDaoJdbc(conexion());
        Tecnico tecnico = tecnicos.insertar(new Tecnico(unico("T")));
        Usuario usuario = new Usuario(unico("tec"), "hash", Rol.TECNICO);
        usuario.setTecnico(tecnico);
        usuarios.insertar(usuario);

        Usuario leido = usuarios.buscarPorNombreUsuario(usuario.getNombreUsuario()).orElseThrow();
        assertEquals(Rol.TECNICO, leido.getRol());
        assertEquals(tecnico.getId(), leido.getTecnico().getId());

        leido.setActivo(false);
        usuarios.actualizar(leido);
        assertFalse(usuarios.buscarPorId(leido.getId()).orElseThrow().isActivo());
    }

    @Test
    void tecnicosActivosExcluyeLosInactivos() {
        TecnicoDao dao = new TecnicoDaoJdbc(conexion());
        Tecnico inactivo = new Tecnico(unico("I"));
        inactivo.setActivo(false);
        dao.insertar(inactivo);
        Tecnico activo = dao.insertar(new Tecnico(unico("A")));

        assertTrue(dao.listarActivos().stream().anyMatch(t -> t.getId().equals(activo.getId())));
        assertTrue(dao.listarActivos().stream().noneMatch(t -> t.getId().equals(inactivo.getId())));
    }

    @Test
    void tipoAtencionServicioYProveedorHacenCrud() {
        TipoAtencionDao tipos = new TipoAtencionDaoJdbc(conexion());
        TipoAtencion tipo = tipos.insertar(new TipoAtencion(unico("T")));
        assertEquals(tipo.getNombre(), tipos.buscarPorId(tipo.getId()).orElseThrow().getNombre());

        ServicioDao servicios = new ServicioDaoJdbc(conexion());
        Servicio servicio = servicios.insertar(new Servicio(unico("Frenos "), new BigDecimal("35.5")));
        assertEquals(new BigDecimal("35.50"), servicios.buscarPorId(servicio.getId()).orElseThrow().getPrecioBase());
        assertEquals(1, servicios.buscar(servicio.getNombre()).size());

        ProveedorDao proveedores = new ProveedorDaoJdbc(conexion());
        Proveedor proveedor = new Proveedor(unico("P"));
        proveedor.setTelefono("123");
        proveedores.insertar(proveedor);
        proveedores.eliminar(proveedor.getId());
        assertTrue(proveedores.buscarPorId(proveedor.getId()).isEmpty());
    }

    @Test
    void repuestoAjustaStockSinPasarseDeCero() {
        RepuestoDao dao = new RepuestoDaoJdbc(conexion());
        Repuesto repuesto = datos.repuesto(5, "9.99");
        repuesto.setStockMinimo(5);
        dao.actualizar(repuesto);

        assertTrue(dao.ajustarStock(repuesto.getId(), -3));
        assertEquals(2, dao.buscarPorId(repuesto.getId()).orElseThrow().getStock());
        assertFalse(dao.ajustarStock(repuesto.getId(), -3));
        assertEquals(2, dao.buscarPorId(repuesto.getId()).orElseThrow().getStock());
        assertTrue(dao.ajustarStock(repuesto.getId(), 10));
        assertEquals(12, dao.buscarPorId(repuesto.getId()).orElseThrow().getStock());
    }

    @Test
    void listarStockBajoYBuscarRepuestos() {
        RepuestoDao dao = new RepuestoDaoJdbc(conexion());
        Repuesto escaso = datos.repuesto(1, "5.00");
        escaso.setStockMinimo(3);
        dao.actualizar(escaso);
        Repuesto sobrado = datos.repuesto(100, "5.00");

        assertTrue(dao.listarStockBajo().stream().anyMatch(r -> r.getId().equals(escaso.getId())));
        assertTrue(dao.listarStockBajo().stream().noneMatch(r -> r.getId().equals(sobrado.getId())));
        assertEquals(1, dao.buscar(sobrado.getCodigo()).size());
    }
}
