package com.taller.api.controlador;

import com.taller.api.seguridad.NoAutenticadoException;
import com.taller.nucleo.excepcion.AccesoDenegadoException;
import com.taller.nucleo.excepcion.PersistenciaException;
import com.taller.nucleo.excepcion.ReglaNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/** Todo error llega al cliente como JSON {"mensaje": "..."} en español, sin detalles internos. */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger REGISTRO = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(NoAutenticadoException.class)
    public ResponseEntity<Map<String, String>> noAutenticado(NoAutenticadoException e) {
        return responder(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    // AccesoDenegadoException hereda de ReglaNegocioException: este manejador es más específico y gana.
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, String>> accesoDenegado(AccesoDenegadoException e) {
        return responder(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, String>> reglaDeNegocio(ReglaNegocioException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> peticionMalFormada(Exception e) {
        return responder(HttpStatus.BAD_REQUEST, "La petición no tiene el formato esperado.");
    }

    @ExceptionHandler(PersistenciaException.class)
    public ResponseEntity<Map<String, String>> persistencia(PersistenciaException e) {
        REGISTRO.error("Error de base de datos", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo completar la operación. Intenta de nuevo más tarde.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> inesperado(Exception e) {
        REGISTRO.error("Error inesperado", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado.");
    }

    private static ResponseEntity<Map<String, String>> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }
}
