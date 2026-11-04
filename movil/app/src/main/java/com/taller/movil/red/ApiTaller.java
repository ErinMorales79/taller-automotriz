package com.taller.movil.red;

import com.taller.movil.red.Modelos.AvanceSolicitud;
import com.taller.movil.red.Modelos.DetalleOrden;
import com.taller.movil.red.Modelos.LoginRespuesta;
import com.taller.movil.red.Modelos.LoginSolicitud;
import com.taller.movil.red.Modelos.Observacion;
import com.taller.movil.red.Modelos.ObservacionSolicitud;
import com.taller.movil.red.Modelos.Tarea;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/** Las rutas de la API del taller (ver docs/api.md). */
public interface ApiTaller {

    @POST("api/auth/login")
    Call<LoginRespuesta> iniciarSesion(@Body LoginSolicitud solicitud);

    @GET("api/ordenes/asignadas")
    Call<List<Tarea>> ordenesAsignadas();

    @GET("api/ordenes/{idOrden}")
    Call<DetalleOrden> detalleDeOrden(@Path("idOrden") int idOrden);

    @PUT("api/asignaciones/{idAsignacion}/avance")
    Call<Tarea> actualizarAvance(@Path("idAsignacion") int idAsignacion, @Body AvanceSolicitud solicitud);

    @POST("api/ordenes/{idOrden}/observaciones")
    Call<Observacion> agregarObservacion(@Path("idOrden") int idOrden, @Body ObservacionSolicitud solicitud);
}
