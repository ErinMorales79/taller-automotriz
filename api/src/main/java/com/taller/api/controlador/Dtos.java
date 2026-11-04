package com.taller.api.controlador;

import com.taller.nucleo.entidad.Asignacion;
import com.taller.nucleo.entidad.Diagnostico;
import com.taller.nucleo.entidad.ObservacionOrden;
import com.taller.nucleo.entidad.OrdenServicio;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/** Lo que entra y sale por la API. Son solo datos: nunca se exponen las entidades del dominio. */
public final class Dtos {

    private Dtos() {
    }

    public record SolicitudLogin(String usuario, String contrasena) {
    }

    public record RespuestaLogin(String token, Instant expiraEn, String usuario, String tecnico) {
    }

    public record SolicitudAvance(int porcentaje) {
    }

    public record SolicitudObservacion(String texto) {
    }

    public record TareaDto(int idAsignacion, int idOrden, String placa, String vehiculo, String tipoAtencion,
                           String prioridad, String estadoOrden, String estadoTarea, int porcentajeAvance,
                           LocalDateTime fechaAsignacion) {
        static TareaDto de(Asignacion asignacion) {
            OrdenServicio orden = asignacion.getOrden();
            return new TareaDto(asignacion.getId(), orden.getId(), orden.getVehiculo().getPlaca(),
                    orden.getVehiculo().getModelo().toString(), orden.getTipoAtencion().getNombre(),
                    orden.getPrioridad().name(), orden.getEstado().getNombre(), asignacion.getEstadoTarea().name(),
                    asignacion.getPorcentajeAvance(), asignacion.getFechaAsignacion());
        }
    }

    public record DiagnosticoDto(String tecnico, String descripcion, LocalDateTime fecha) {
        static DiagnosticoDto de(Diagnostico diagnostico) {
            return new DiagnosticoDto(diagnostico.getTecnico().getNombre(), diagnostico.getDescripcion(), diagnostico.getFecha());
        }
    }

    public record ObservacionDto(int id, String tecnico, String texto, LocalDateTime fecha) {
        static ObservacionDto de(ObservacionOrden observacion) {
            return new ObservacionDto(observacion.getId(), observacion.getTecnico().getNombre(),
                    observacion.getTexto(), observacion.getFecha());
        }
    }

    public record DetalleOrdenDto(int idOrden, String placa, String vehiculo, int anio, String color,
                                  String tipoAtencion, String prioridad, String estado, LocalDateTime fechaIngreso,
                                  TareaDto miTarea, List<DiagnosticoDto> diagnosticos,
                                  List<ObservacionDto> observaciones) {
    }
}
