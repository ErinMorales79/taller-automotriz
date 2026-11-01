package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.jdbc.AuditoriaDaoJdbc;
import com.taller.nucleo.dao.jdbc.UsuarioDaoJdbc;
import com.taller.nucleo.entidad.Asignacion;
import com.taller.nucleo.entidad.Auditoria;
import com.taller.nucleo.entidad.Compra;
import com.taller.nucleo.entidad.MetodoPago;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Prioridad;
import com.taller.nucleo.entidad.Proveedor;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.AccesoDenegadoException;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import com.taller.nucleo.seguridad.Accion;
import com.taller.nucleo.seguridad.Permisos;
import com.taller.nucleo.soporte.BaseBD;
import com.taller.nucleo.soporte.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Autenticación, permisos por rol y auditoría. */
class SeguridadYAuditoriaTest extends BaseBD {

    private static final String CONTRASENA = "clave-segura-1";

    private final UsuarioServicio usuarios = new UsuarioServicio();
    private final AutenticacionServicio autenticacion = new AutenticacionServicio();
    private final AuditoriaServicio auditoria = new AuditoriaServicio();
    private final OrdenServicioServicio ordenes = new OrdenServicioServicio();
    private final TareaServicio tareas = new TareaServicio();

    private Datos datos;
    private Usuario admin;
    private Usuario recepcion;

    @BeforeEach
    void preparar() {
        datos = new Datos(conexion());
        admin = datos.usuario(Rol.ADMINISTRADOR);
        recepcion = datos.usuario(Rol.RECEPCIONISTA);
    }

    private List<String> accionesDe(Usuario usuario) {
        return new AuditoriaDaoJdbc(conexion()).listarPorUsuario(usuario.getId()).stream()
                .map(Auditoria::getAccion).toList();
    }

    @Nested
    class MatrizDePermisos {

        @Test
        void elAdministradorPuedeTodo() {
            for (Accion accion : Accion.values()) {
                assertTrue(Permisos.puede(admin, accion), accion.name());
            }
        }

        @Test
        void cadaRolSoloPuedeLoSuyo() {
            Usuario tecnico = datos.usuario(Rol.TECNICO);
            Usuario encargado = datos.usuario(Rol.ENCARGADO_INVENTARIO);

            assertTrue(Permisos.puede(recepcion, Accion.CREAR_ORDEN));
            assertFalse(Permisos.puede(recepcion, Accion.GESTIONAR_USUARIOS));
            assertFalse(Permisos.puede(recepcion, Accion.GESTIONAR_INVENTARIO));
            assertTrue(Permisos.puede(tecnico, Accion.ACTUALIZAR_AVANCE));
            assertFalse(Permisos.puede(tecnico, Accion.REGISTRAR_PAGO));
            assertTrue(Permisos.puede(encargado, Accion.GESTIONAR_INVENTARIO));
            assertFalse(Permisos.puede(encargado, Accion.GESTIONAR_ORDEN));
            assertFalse(Permisos.puede(recepcion, Accion.AUTORIZAR_EXCEPCIONES));
        }

        @Test
        void unUsuarioDesactivadoNoPuedeNada() {
            recepcion.setActivo(false);
            AccesoDenegadoException error = assertThrows(AccesoDenegadoException.class,
                    () -> Permisos.exigir(recepcion, Accion.CREAR_ORDEN));
            assertTrue(error.getMessage().contains("desactivado"));
        }

        @Test
        void exigirSinPermisoExplicaElMotivo() {
            AccesoDenegadoException error = assertThrows(AccesoDenegadoException.class,
                    () -> Permisos.exigir(recepcion, Accion.GESTIONAR_USUARIOS));
            assertTrue(error.getMessage().contains("RECEPCIONISTA"));
        }
    }

    @Nested
    class InicioDeSesion {

        @Test
        void conCredencialesCorrectasEntraYLaContrasenaNoSeGuardaEnClaro() {
            usuarios.crear(admin, "maria", CONTRASENA, Rol.RECEPCIONISTA, null);

            Usuario sesion = autenticacion.iniciarSesion("maria", CONTRASENA);

            assertEquals(Rol.RECEPCIONISTA, sesion.getRol());
            assertNotEquals(CONTRASENA, sesion.getContrasenaHash());
            assertTrue(sesion.getContrasenaHash().startsWith("$2"), "Debe ser un hash BCrypt");
        }

        @Test
        void contrasenaIncorrectaYUsuarioInexistenteDanElMismoMensaje() {
            String usuario = unico("u");
            usuarios.crear(admin, usuario, CONTRASENA, Rol.RECEPCIONISTA, null);

            AccesoDenegadoException malaClave = assertThrows(AccesoDenegadoException.class,
                    () -> autenticacion.iniciarSesion(usuario, "otra-clave-xx"));
            AccesoDenegadoException noExiste = assertThrows(AccesoDenegadoException.class,
                    () -> autenticacion.iniciarSesion(unico("fantasma"), CONTRASENA));
            assertEquals(malaClave.getMessage(), noExiste.getMessage());
        }

        @Test
        void camposVaciosSeRechazan() {
            assertThrows(AccesoDenegadoException.class, () -> autenticacion.iniciarSesion(null, null));
            assertThrows(AccesoDenegadoException.class, () -> autenticacion.iniciarSesion("  ", ""));
        }

        @Test
        void unUsuarioDesactivadoNoEntra() {
            Usuario creado = usuarios.crear(admin, unico("u"), CONTRASENA, Rol.RECEPCIONISTA, null);
            usuarios.desactivar(admin, creado.getId());

            assertThrows(AccesoDenegadoException.class,
                    () -> autenticacion.iniciarSesion(creado.getNombreUsuario(), CONTRASENA));
        }

        @Test
        void tantoElExitoComoElFalloQuedanAuditados() {
            Usuario creado = usuarios.crear(admin, unico("u"), CONTRASENA, Rol.RECEPCIONISTA, null);

            autenticacion.iniciarSesion(creado.getNombreUsuario(), CONTRASENA);
            assertThrows(AccesoDenegadoException.class,
                    () -> autenticacion.iniciarSesion(creado.getNombreUsuario(), "incorrecta-1"));

            assertEquals(List.of("LOGIN_FALLIDO", "LOGIN"), accionesDe(creado),
                    "El fallo se audita aunque la operación termine en error");
        }
    }

    @Nested
    class GestionDeUsuarios {

        @Test
        void soloUnAdministradorCreaUsuarios() {
            assertThrows(AccesoDenegadoException.class,
                    () -> usuarios.crear(recepcion, unico("u"), CONTRASENA, Rol.RECEPCIONISTA, null));
        }

        @Test
        void laContrasenaCortaSeRechazaYNoSeCreaNada() {
            String nombre = unico("u");
            assertThrows(ReglaNegocioException.class,
                    () -> usuarios.crear(admin, nombre, "corta", Rol.RECEPCIONISTA, null));
            assertTrue(new UsuarioDaoJdbc(conexion()).buscarPorNombreUsuario(nombre).isEmpty());
        }

        @Test
        void unUsuarioTecnicoExigeUnTecnicoGuardado() {
            assertThrows(ReglaNegocioException.class,
                    () -> usuarios.crear(admin, unico("u"), CONTRASENA, Rol.TECNICO, null));
            Usuario conTecnico = usuarios.crear(admin, unico("u"), CONTRASENA, Rol.TECNICO, datos.tecnico());
            assertTrue(conTecnico.getTecnico().getId() > 0);
        }

        @Test
        void cambiarContrasenaExigeLaActual() {
            Usuario creado = usuarios.crear(admin, unico("u"), CONTRASENA, Rol.RECEPCIONISTA, null);

            assertThrows(ReglaNegocioException.class,
                    () -> usuarios.cambiarContrasena(creado, "no-es-esta-1", "nueva-clave-2"));
            usuarios.cambiarContrasena(creado, CONTRASENA, "nueva-clave-2");

            assertEquals(creado.getRol(), autenticacion.iniciarSesion(creado.getNombreUsuario(), "nueva-clave-2").getRol());
            assertThrows(AccesoDenegadoException.class,
                    () -> autenticacion.iniciarSesion(creado.getNombreUsuario(), CONTRASENA));
        }

        @Test
        void nadieSeDesactivaASiMismo() {
            assertThrows(ReglaNegocioException.class, () -> usuarios.desactivar(admin, admin.getId()));
        }
    }

    @Nested
    class PermisosEnLosServicios {

        @Test
        void unTecnicoNoPuedeCrearOrdenesNiRegistrarPagos() {
            Usuario tecnico = datos.usuario(Rol.TECNICO);
            assertThrows(AccesoDenegadoException.class, () -> ordenes.crear(
                    datos.vehiculo(), datos.tipoAtencion(), Prioridad.BAJA, tecnico, null));
            assertThrows(AccesoDenegadoException.class, () -> new PagoServicio().registrar(
                    1, BigDecimal.ONE, MetodoPago.EFECTIVO, tecnico));
        }

        @Test
        void soloElEncargadoOElAdministradorRegistranCompras() {
            Proveedor proveedor = new com.taller.nucleo.dao.jdbc.ProveedorDaoJdbc(conexion())
                    .insertar(new Proveedor(unico("Prov ")));
            Compra compraDeRecepcion = new Compra(proveedor, recepcion);
            compraDeRecepcion.agregarDetalle(datos.repuesto(0, "5.00"), 1, BigDecimal.ONE);

            assertThrows(AccesoDenegadoException.class,
                    () -> new InventarioServicio().registrarCompra(compraDeRecepcion));
        }

        @Test
        void unEncargadoDeInventarioNoCambiaEstadosDeOrdenes() {
            OrdenServicio orden = datos.orden(recepcion);
            Usuario encargado = datos.usuario(Rol.ENCARGADO_INVENTARIO);
            assertThrows(AccesoDenegadoException.class, () -> ordenes.iniciarDiagnostico(orden.getId(), encargado));
        }
    }

    @Nested
    class Auditoria_ {

        @Test
        void crearYCambiarUnaOrdenQuedaAuditadoConElResponsable() {
            OrdenServicio orden = ordenes.crear(datos.vehiculo(), datos.tipoAtencion(), Prioridad.MEDIA, recepcion, null);
            ordenes.iniciarDiagnostico(orden.getId(), recepcion);

            assertEquals(List.of("ORDEN_EN_DIAGNOSTICO", "CREAR_ORDEN"), accionesDe(recepcion));
            Auditoria registro = new AuditoriaDaoJdbc(conexion()).listarPorUsuario(recepcion.getId()).get(1);
            assertEquals("orden_servicio", registro.getTablaAfectada());
            assertEquals(orden.getId(), registro.getIdRegistro());
        }

        @Test
        void unaOperacionRechazadaNoDejaAuditoria() {
            OrdenServicio orden = ordenes.crear(datos.vehiculo(), datos.tipoAtencion(), Prioridad.MEDIA, recepcion, null);
            assertThrows(ReglaNegocioException.class, () -> ordenes.finalizar(orden.getId(), recepcion));
            assertEquals(List.of("CREAR_ORDEN"), accionesDe(recepcion));
        }

        @Test
        void soloQuienTienePermisoConsultaLaAuditoria() {
            ordenes.crear(datos.vehiculo(), datos.tipoAtencion(), Prioridad.MEDIA, recepcion, null);

            assertThrows(AccesoDenegadoException.class, () -> auditoria.consultarPorTabla(recepcion, "orden_servicio"));
            assertFalse(auditoria.consultarPorUsuario(admin, recepcion.getId()).isEmpty());
        }

        @Test
        void autorizarExcepcionesQuedaRegistradoAFavorDelAdministrador() {
            var vehiculo = datos.vehiculo();
            var tipo = datos.tipoAtencion();
            ordenes.crear(vehiculo, tipo, Prioridad.MEDIA, recepcion, null);
            ordenes.crear(vehiculo, tipo, Prioridad.MEDIA, recepcion, admin);

            assertTrue(accionesDe(admin).contains("AUTORIZAR_DUPLICADA"));
        }
    }

    @Nested
    class AsignacionDeTareas {

        @Test
        void recepcionAsignaYElTecnicoAsignadoActualizaSuAvance() {
            Tecnico tecnico = datos.tecnico();
            Usuario usuarioTecnico = usuarios.crear(admin, unico("tec"), CONTRASENA, Rol.TECNICO, tecnico);
            OrdenServicio orden = datos.orden(recepcion);

            Asignacion asignacion = tareas.asignar(orden.getId(), tecnico.getId(), recepcion);
            Asignacion actualizada = tareas.actualizarAvance(asignacion.getId(), 50, usuarioTecnico);

            assertEquals(50, actualizada.getPorcentajeAvance());
        }

        @Test
        void otroTecnicoNoPuedeTocarLaTareaAjena() {
            Tecnico dueno = datos.tecnico();
            Usuario otro = usuarios.crear(admin, unico("tec"), CONTRASENA, Rol.TECNICO, datos.tecnico());
            OrdenServicio orden = datos.orden(recepcion);
            Asignacion asignacion = tareas.asignar(orden.getId(), dueno.getId(), recepcion);

            assertThrows(AccesoDenegadoException.class, () -> tareas.actualizarAvance(asignacion.getId(), 10, otro));
        }

        @Test
        void noSeAsignaUnTecnicoInactivoNiSeAsignaSinPermiso() {
            Tecnico inactivo = datos.tecnico();
            inactivo.setActivo(false);
            new com.taller.nucleo.dao.jdbc.TecnicoDaoJdbc(conexion()).actualizar(inactivo);
            OrdenServicio orden = datos.orden(recepcion);
            Usuario tecnico = datos.usuario(Rol.TECNICO);

            assertThrows(ReglaNegocioException.class, () -> tareas.asignar(orden.getId(), inactivo.getId(), recepcion));
            assertThrows(AccesoDenegadoException.class, () -> tareas.asignar(orden.getId(), datos.tecnico().getId(), tecnico));
        }
    }
}
