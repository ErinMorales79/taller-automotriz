package com.taller.movil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.taller.movil.red.ApiTaller;
import com.taller.movil.red.ClienteApi;
import com.taller.movil.red.Modelos.AvanceSolicitud;
import com.taller.movil.red.Modelos.DetalleOrden;
import com.taller.movil.red.Modelos.LoginRespuesta;
import com.taller.movil.red.Modelos.LoginSolicitud;
import com.taller.movil.red.Modelos.Observacion;
import com.taller.movil.red.Modelos.ObservacionSolicitud;
import com.taller.movil.red.Modelos.Tarea;
import com.taller.movil.red.Resultado;
import com.taller.movil.util.Servidor;
import com.taller.movil.util.Texto;

import java.io.IOException;
import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Call;

/** La capa de red contra un servidor simulado: respuestas de la API (docs/api.md), errores y cabecera del token. */
class RedTest {

    private MockWebServer servidor;
    private String token;
    private ApiTaller api;

    @BeforeEach
    void iniciar() throws IOException {
        servidor = new MockWebServer();
        servidor.start();
        token = null;
        api = ClienteApi.crear(servidor.url("/").toString(), () -> token);
    }

    @AfterEach
    void detener() throws IOException {
        servidor.shutdown();
    }

    private static <T> Resultado<T> resolver(Call<T> llamada) {
        try {
            return Resultado.deRespuesta(llamada.execute());
        } catch (IOException e) {
            return Resultado.deFallo(e);
        }
    }

    private void responder(int codigo, String cuerpo) {
        servidor.enqueue(new MockResponse().setResponseCode(codigo).setHeader("Content-Type", "application/json").setBody(cuerpo));
    }

    // ---------- respuestas correctas ----------

    @Test
    void elLoginEntregaElTokenYNoLlevaCabeceraDeAutorizacion() throws Exception {
        responder(200, "{\"token\":\"abc.def.ghi\",\"expiraEn\":\"2026-11-02T04:00:00Z\",\"usuario\":\"tecnico1\",\"tecnico\":\"Luis Ramírez\"}");

        Resultado<LoginRespuesta> resultado = resolver(api.iniciarSesion(new LoginSolicitud("tecnico1", "Tecnico2026")));

        assertTrue(resultado.esExito());
        assertEquals("abc.def.ghi", resultado.getDato().getToken());
        assertEquals("Luis Ramírez", resultado.getDato().getTecnico());
        RecordedRequest peticion = servidor.takeRequest();
        assertEquals("/api/auth/login", peticion.getPath());
        assertNull(peticion.getHeader("Authorization"));
        assertTrue(peticion.getBody().readUtf8().contains("\"usuario\":\"tecnico1\""));
    }

    @Test
    void conSesionCadaLlamadaLlevaElTokenYLaListaSeLeeBien() throws Exception {
        token = "mi-token";
        responder(200, "[{\"idAsignacion\":12,\"idOrden\":5,\"placa\":\"AAA-1001\",\"vehiculo\":\"Toyota Corolla\","
                + "\"tipoAtencion\":\"Reparación de frenos\",\"prioridad\":\"ALTA\",\"estadoOrden\":\"EN_REPARACION\","
                + "\"estadoTarea\":\"EN_CURSO\",\"porcentajeAvance\":40,\"fechaAsignacion\":\"2026-10-29T18:24:00\"}]");

        Resultado<List<Tarea>> resultado = resolver(api.ordenesAsignadas());

        assertEquals(1, resultado.getDato().size());
        Tarea tarea = resultado.getDato().get(0);
        assertEquals(12, tarea.getIdAsignacion());
        assertEquals("AAA-1001", tarea.getPlaca());
        assertEquals(40, tarea.getPorcentajeAvance());
        assertEquals("Bearer mi-token", servidor.takeRequest().getHeader("Authorization"));
    }

    @Test
    void elDetalleTraeDiagnosticosYObservacionesAunqueFalten() {
        responder(200, "{\"idOrden\":5,\"placa\":\"AAA-1001\",\"vehiculo\":\"Toyota Corolla\",\"anio\":2019,\"color\":null,"
                + "\"tipoAtencion\":\"Frenos\",\"prioridad\":\"ALTA\",\"estado\":\"EN_REPARACION\",\"fechaIngreso\":\"2026-10-29T18:24:00\","
                + "\"miTarea\":{\"idAsignacion\":12,\"idOrden\":5,\"porcentajeAvance\":40},"
                + "\"diagnosticos\":[{\"tecnico\":\"Luis\",\"descripcion\":\"Pastillas gastadas\",\"fecha\":\"2026-10-29T19:00:00\"}]}");

        DetalleOrden detalle = resolver(api.detalleDeOrden(5)).getDato();

        assertEquals(1, detalle.getDiagnosticos().size());
        assertTrue(detalle.getObservaciones().isEmpty(), "Sin la lista en el JSON se muestra vacía, no null");
        assertEquals(12, detalle.getMiTarea().getIdAsignacion());
    }

    @Test
    void elAvanceYLaObservacionSeEnvianComoJson() throws Exception {
        token = "t";
        responder(200, "{\"idAsignacion\":12,\"porcentajeAvance\":60}");
        responder(201, "{\"id\":9,\"tecnico\":\"Luis\",\"texto\":\"Listo\",\"fecha\":\"2026-10-30T10:00:00\"}");

        assertEquals(60, resolver(api.actualizarAvance(12, new AvanceSolicitud(60))).getDato().getPorcentajeAvance());
        RecordedRequest avance = servidor.takeRequest();
        assertEquals("PUT", avance.getMethod());
        assertEquals("/api/asignaciones/12/avance", avance.getPath());
        assertEquals("{\"porcentaje\":60}", avance.getBody().readUtf8());

        Observacion observacion = resolver(api.agregarObservacion(5, new ObservacionSolicitud("Listo"))).getDato();
        assertEquals("Listo", observacion.getTexto());
        assertEquals("/api/ordenes/5/observaciones", servidor.takeRequest().getPath());
    }

    // ---------- errores ----------

    @Test
    void elMensajeDeLaApiSeMuestraTalCualYUn401MarcaLaSesionVencida() {
        responder(401, "{\"mensaje\":\"Usuario o contraseña incorrectos.\"}");

        Resultado<LoginRespuesta> resultado = resolver(api.iniciarSesion(new LoginSolicitud("x", "y")));

        assertFalse(resultado.esExito());
        assertEquals("Usuario o contraseña incorrectos.", resultado.getMensaje());
        assertTrue(resultado.isSesionVencida());
    }

    @Test
    void lasReglasDeNegocioYLosPermisosNoCierranLaSesion() {
        responder(400, "{\"mensaje\":\"El porcentaje debe estar entre 0 y 100.\"}");
        responder(403, "{\"mensaje\":\"La orden #7 no está asignada a este técnico.\"}");

        Resultado<Tarea> regla = resolver(api.actualizarAvance(1, new AvanceSolicitud(150)));
        Resultado<DetalleOrden> permiso = resolver(api.detalleDeOrden(7));

        assertEquals("El porcentaje debe estar entre 0 y 100.", regla.getMensaje());
        assertFalse(regla.isSesionVencida());
        assertEquals("La orden #7 no está asignada a este técnico.", permiso.getMensaje());
        assertFalse(permiso.isSesionVencida());
    }

    @Test
    void unErrorSinJsonUsaUnMensajeGenericoEnEspanol() {
        servidor.enqueue(new MockResponse().setResponseCode(502).setBody("<html>Bad Gateway</html>"));

        Resultado<List<Tarea>> resultado = resolver(api.ordenesAsignadas());

        assertEquals("El servidor respondió con un error inesperado. Intenta de nuevo.", resultado.getMensaje());
    }

    @Test
    void sinConexionConElServidorElMensajeEsClaro() throws IOException {
        servidor.shutdown();

        Resultado<List<Tarea>> resultado = resolver(api.ordenesAsignadas());

        assertEquals("No se pudo conectar con el servidor. Revisa la dirección y tu conexión.", resultado.getMensaje());
        assertFalse(resultado.isSesionVencida());
    }

    // ---------- ayudas ----------

    @Test
    void laDireccionDelServidorSeNormalizaOSeRechaza() {
        assertEquals("http://192.168.1.20:8080/", Servidor.normalizar("192.168.1.20:8080"));
        assertEquals("http://10.0.2.2:8080/", Servidor.normalizar("  http://10.0.2.2:8080  "));
        assertEquals("https://taller.ejemplo.com/", Servidor.normalizar("https://taller.ejemplo.com"));
        assertThrows(IllegalArgumentException.class, () -> Servidor.normalizar("   "));
        assertThrows(IllegalArgumentException.class, () -> Servidor.normalizar("http://"));
    }

    @Test
    void losTextosSeMuestranEnEspanolLegible() {
        assertEquals("En reparación", Texto.legible("EN_REPARACION"));
        assertEquals("Esperando aprobación", Texto.legible("ESPERANDO_APROBACION"));
        assertEquals("-", Texto.legible(null));
        assertEquals("29/10/2026 18:24", Texto.fechaHora("2026-10-29T18:24:00"));
        assertEquals("sin fecha", Texto.fechaHora("sin fecha"));
        assertEquals("-", Texto.fechaHora(null));
    }
}
