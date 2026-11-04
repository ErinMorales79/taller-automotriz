package com.taller.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.taller.api.seguridad.ServicioToken;
import com.taller.nucleo.entidad.Asignacion;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.servicio.OrdenServicioServicio;
import com.taller.nucleo.servicio.TareaServicio;
import com.taller.nucleo.servicio.UsuarioServicio;
import com.taller.nucleo.soporte.BaseBD;
import com.taller.nucleo.soporte.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.Date;

import static com.taller.nucleo.soporte.Datos.unico;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** La API completa (filtro, controladores y errores) contra la base de pruebas, con rollback al final. */
@SpringBootTest(properties = {"taller.jwt.secreto=secreto-de-pruebas-que-tiene-mas-de-32-caracteres", "taller.jwt.horas=1"})
@AutoConfigureMockMvc
class ApiTecnicosTest extends BaseBD {

    private static final String CONTRASENA = "Clave12345";
    private static final String SECRETO = "secreto-de-pruebas-que-tiene-mas-de-32-caracteres";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ServicioToken tokens;

    private final OrdenServicioServicio ordenes = new OrdenServicioServicio();
    private final TareaServicio tareas = new TareaServicio();

    private Datos datos;
    private Usuario recepcion;
    private Usuario admin;
    private Usuario usuarioTecnico;
    private String nombreTecnico;
    private OrdenServicio orden;
    private Asignacion tarea;

    @BeforeEach
    void preparar() {
        datos = new Datos(conexion());
        recepcion = datos.usuario(Rol.RECEPCIONISTA);
        admin = datos.usuario(Rol.ADMINISTRADOR);
        Tecnico tecnico = datos.tecnico();
        nombreTecnico = unico("tec");
        usuarioTecnico = new UsuarioServicio().crear(admin, nombreTecnico, CONTRASENA, Rol.TECNICO, tecnico);
        orden = datos.orden(recepcion);
        tarea = tareas.asignar(orden.getId(), tecnico.getId(), recepcion);
    }

    private String tokenDelTecnico() {
        return tokens.emitir(usuarioTecnico).token();
    }

    private ResultActions con(MockHttpServletRequestBuilder peticion, String token) throws Exception {
        return mvc.perform(peticion.header("Authorization", "Bearer " + token));
    }

    private ResultActions login(String usuario, String contrasena) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":\"" + usuario + "\",\"contrasena\":\"" + contrasena + "\"}"));
    }

    // ---------- inicio de sesión ----------

    @Test
    void elTecnicoIniciaSesionYRecibeUnTokenQueLeAbreLaApi() throws Exception {
        String respuesta = login(nombreTecnico, CONTRASENA).andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.usuario").value(nombreTecnico))
                .andReturn().getResponse().getContentAsString();
        String token = respuesta.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        con(get("/api/ordenes/asignadas"), token).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idOrden").value(orden.getId()))
                .andExpect(jsonPath("$[0].porcentajeAvance").value(0));
    }

    @Test
    void lasCredencialesMalasDanElMismoMensajeSeaCualSeaElMotivo() throws Exception {
        login(nombreTecnico, "ClaveEquivocada").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos."));
        login("no-existe-" + nombreTecnico, CONTRASENA).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos."));
    }

    @Test
    void unUsuarioQueNoEsTecnicoNoRecibeToken() throws Exception {
        String nombre = unico("rec");
        new UsuarioServicio().crear(admin, nombre, CONTRASENA, Rol.RECEPCIONISTA, null);
        login(nombre, CONTRASENA).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo los técnicos pueden usar la app móvil."));
    }

    @Test
    void unCuerpoMalFormadoDaUnErrorClaro() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La petición no tiene el formato esperado."));
    }

    // ---------- token ----------

    @Test
    void sinTokenOConTokenInvalidoLaApiRespondeNoAutenticado() throws Exception {
        mvc.perform(get("/api/ordenes/asignadas")).andExpect(status().isUnauthorized());
        con(get("/api/ordenes/asignadas"), "token.falso.cualquiera").andExpect(status().isUnauthorized());
        String manipulado = tokenDelTecnico().replaceAll(".$", "x");
        con(get("/api/ordenes/asignadas"), manipulado).andExpect(status().isUnauthorized());
    }

    @Test
    void unTokenVencidoOFirmadoConOtroSecretoSeRechaza() throws Exception {
        String vencido = JWT.create().withSubject(String.valueOf(usuarioTecnico.getId()))
                .withExpiresAt(Date.from(Instant.now().minusSeconds(60))).sign(Algorithm.HMAC256(SECRETO));
        con(get("/api/ordenes/asignadas"), vencido).andExpect(status().isUnauthorized());
        String ajeno = JWT.create().withSubject(String.valueOf(usuarioTecnico.getId()))
                .withExpiresAt(Date.from(Instant.now().plusSeconds(600))).sign(Algorithm.HMAC256("otro-secreto-distinto-de-mas-de-32-caracteres"));
        con(get("/api/ordenes/asignadas"), ajeno).andExpect(status().isUnauthorized());
    }

    @Test
    void elTokenDeUnUsuarioDesactivadoOQueNoEsTecnicoDejaDeServir() throws Exception {
        String tokenRecepcion = tokens.emitir(recepcion).token();
        con(get("/api/ordenes/asignadas"), tokenRecepcion).andExpect(status().isForbidden());

        String tokenTecnico = tokenDelTecnico();
        new UsuarioServicio().desactivar(admin, usuarioTecnico.getId());
        con(get("/api/ordenes/asignadas"), tokenTecnico).andExpect(status().isUnauthorized());
    }

    @Test
    void elSecretoCortoOAusenteImpideArrancar() {
        assertThrows(IllegalStateException.class, () -> new ServicioToken("corto", 8));
        assertThrows(IllegalStateException.class, () -> new ServicioToken("", 8));
        assertThrows(IllegalStateException.class, () -> new ServicioToken(SECRETO, 0));
    }

    // ---------- órdenes, avance y observaciones ----------

    @Test
    void elDetalleDeLaOrdenPropiaSeMuestraYLaAjenaEstaProhibida() throws Exception {
        con(get("/api/ordenes/" + orden.getId()), tokenDelTecnico()).andExpect(status().isOk())
                .andExpect(jsonPath("$.placa").value(orden.getVehiculo().getPlaca()))
                .andExpect(jsonPath("$.miTarea.idAsignacion").value(tarea.getId()));

        OrdenServicio ajena = datos.orden(recepcion);
        tareas.asignar(ajena.getId(), datos.tecnico().getId(), recepcion);
        con(get("/api/ordenes/" + ajena.getId()), tokenDelTecnico()).andExpect(status().isForbidden());
    }

    @Test
    void elTecnicoActualizaElAvanceDeSuTareaYNoDeLaAjena() throws Exception {
        con(put("/api/asignaciones/" + tarea.getId() + "/avance").contentType(MediaType.APPLICATION_JSON)
                .content("{\"porcentaje\":60}"), tokenDelTecnico()).andExpect(status().isOk())
                .andExpect(jsonPath("$.porcentajeAvance").value(60));

        OrdenServicio ajena = datos.orden(recepcion);
        Asignacion tareaAjena = tareas.asignar(ajena.getId(), datos.tecnico().getId(), recepcion);
        con(put("/api/asignaciones/" + tareaAjena.getId() + "/avance").contentType(MediaType.APPLICATION_JSON)
                .content("{\"porcentaje\":10}"), tokenDelTecnico()).andExpect(status().isForbidden());
    }

    @Test
    void unAvanceFueraDeRangoSeRechazaConUnMensajeDeNegocio() throws Exception {
        con(put("/api/asignaciones/" + tarea.getId() + "/avance").contentType(MediaType.APPLICATION_JSON)
                .content("{\"porcentaje\":150}"), tokenDelTecnico()).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", not(emptyOrNullString())));
    }

    @Test
    void elTecnicoAgregaObservacionesYAparecenEnElDetalle() throws Exception {
        con(post("/api/ordenes/" + orden.getId() + "/observaciones").contentType(MediaType.APPLICATION_JSON)
                .content("{\"texto\":\"Se cambió el filtro de aceite\"}"), tokenDelTecnico())
                .andExpect(status().isCreated()).andExpect(jsonPath("$.texto").value("Se cambió el filtro de aceite"));

        con(get("/api/ordenes/" + orden.getId()), tokenDelTecnico())
                .andExpect(jsonPath("$.observaciones", hasSize(1)));
    }

    @Test
    void noSeAceptanObservacionesVaciasNiEnOrdenesCerradasNiAjenas() throws Exception {
        con(post("/api/ordenes/" + orden.getId() + "/observaciones").contentType(MediaType.APPLICATION_JSON)
                .content("{\"texto\":\"  \"}"), tokenDelTecnico()).andExpect(status().isBadRequest());

        OrdenServicio ajena = datos.orden(recepcion);
        tareas.asignar(ajena.getId(), datos.tecnico().getId(), recepcion);
        con(post("/api/ordenes/" + ajena.getId() + "/observaciones").contentType(MediaType.APPLICATION_JSON)
                .content("{\"texto\":\"Intruso\"}"), tokenDelTecnico()).andExpect(status().isForbidden());

        ordenes.cancelar(orden.getId(), recepcion);
        con(post("/api/ordenes/" + orden.getId() + "/observaciones").contentType(MediaType.APPLICATION_JSON)
                .content("{\"texto\":\"Tarde\"}"), tokenDelTecnico()).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se pueden agregar observaciones a una orden cerrada."));
    }
}
