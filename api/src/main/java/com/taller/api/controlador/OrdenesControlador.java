package com.taller.api.controlador;

import com.taller.api.controlador.Dtos.DetalleOrdenDto;
import com.taller.api.controlador.Dtos.DiagnosticoDto;
import com.taller.api.controlador.Dtos.ObservacionDto;
import com.taller.api.controlador.Dtos.SolicitudAvance;
import com.taller.api.controlador.Dtos.SolicitudObservacion;
import com.taller.api.controlador.Dtos.TareaDto;
import com.taller.api.seguridad.FiltroJwt;
import com.taller.nucleo.entidad.Asignacion;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.servicio.ConsultaServicio;
import com.taller.nucleo.servicio.TareaServicio;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Lo que el técnico hace desde el móvil: ver sus órdenes, actualizar el avance y dejar observaciones. */
@RestController
@RequestMapping("/api")
public class OrdenesControlador {

    private final TareaServicio tareas = new TareaServicio();
    private final ConsultaServicio consultas = new ConsultaServicio();

    @GetMapping("/ordenes/asignadas")
    public List<TareaDto> ordenesAsignadas(@RequestAttribute(FiltroJwt.ATRIBUTO_USUARIO) Usuario usuario) {
        return tareas.asignacionesDelTecnico(usuario).stream().map(TareaDto::de).toList();
    }

    @GetMapping("/ordenes/{idOrden}")
    public DetalleOrdenDto detalle(@PathVariable int idOrden, @RequestAttribute(FiltroJwt.ATRIBUTO_USUARIO) Usuario usuario) {
        OrdenServicio orden = tareas.ordenAsignada(idOrden, usuario);
        Asignacion miTarea = tareas.asignacionesDelTecnico(usuario).stream()
                .filter(a -> a.getOrden().getId().equals(idOrden)).findFirst().orElseThrow();
        return new DetalleOrdenDto(orden.getId(), orden.getVehiculo().getPlaca(), orden.getVehiculo().getModelo().toString(),
                orden.getVehiculo().getAnio(), orden.getVehiculo().getColor(), orden.getTipoAtencion().getNombre(),
                orden.getPrioridad().name(), orden.getEstado().getNombre(), orden.getFechaIngreso(), TareaDto.de(miTarea),
                consultas.diagnosticos(idOrden).stream().map(DiagnosticoDto::de).toList(),
                consultas.observaciones(idOrden).stream().map(ObservacionDto::de).toList());
    }

    @PostMapping("/ordenes/{idOrden}/observaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public ObservacionDto agregarObservacion(@PathVariable int idOrden, @RequestBody SolicitudObservacion solicitud,
                                             @RequestAttribute(FiltroJwt.ATRIBUTO_USUARIO) Usuario usuario) {
        return ObservacionDto.de(tareas.agregarObservacion(idOrden, solicitud.texto(), usuario));
    }

    @PutMapping("/asignaciones/{idAsignacion}/avance")
    public TareaDto actualizarAvance(@PathVariable int idAsignacion, @RequestBody SolicitudAvance solicitud,
                                     @RequestAttribute(FiltroJwt.ATRIBUTO_USUARIO) Usuario usuario) {
        return TareaDto.de(tareas.actualizarAvance(idAsignacion, solicitud.porcentaje(), usuario));
    }
}
