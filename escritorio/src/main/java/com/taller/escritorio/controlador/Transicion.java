package com.taller.escritorio.controlador;

import com.taller.nucleo.seguridad.Accion;

import java.util.List;

/**
 * Cambios de estado que el usuario puede pedir desde el detalle de una orden. Los estados desde los
 * que se ofrece cada uno reflejan las transiciones del patrón State; la regla real la valida nucleo.
 */
public enum Transicion {
    INICIAR_DIAGNOSTICO("Iniciar diagnóstico", "check", Accion.GESTIONAR_ORDEN, null),
    ENVIAR_A_APROBACION("Enviar a aprobación", "check", Accion.GESTIONAR_ORDEN, null),
    INICIAR_REPARACION("Iniciar reparación", "check", Accion.GESTIONAR_ORDEN, null),
    VOLVER_A_DIAGNOSTICO("Volver a diagnóstico", "refrescar", Accion.GESTIONAR_ORDEN,
            "¿Volver la orden a diagnóstico para armar un presupuesto nuevo?"),
    FINALIZAR("Finalizar trabajo", "check", Accion.FINALIZAR_ORDEN, "¿Marcar el trabajo como finalizado?"),
    ENTREGAR("Entregar vehículo", "auto", Accion.GESTIONAR_ORDEN, "¿Entregar el vehículo al cliente?"),
    CANCELAR("Cancelar orden", "cerrar", Accion.GESTIONAR_ORDEN, "¿Cancelar la orden? Esta acción no se puede deshacer.");

    private final String texto;
    private final String icono;
    private final Accion permiso;
    private final String confirmacion;

    Transicion(String texto, String icono, Accion permiso, String confirmacion) {
        this.texto = texto;
        this.icono = icono;
        this.permiso = permiso;
        this.confirmacion = confirmacion;
    }

    public String texto() {
        return texto;
    }

    public String icono() {
        return icono;
    }

    public Accion permiso() {
        return permiso;
    }

    /** Pregunta previa al cambio, o null si no necesita confirmación. */
    public String confirmacion() {
        return confirmacion;
    }

    public boolean esPeligrosa() {
        return this == CANCELAR;
    }

    public static List<Transicion> desde(String estado) {
        return switch (estado) {
            case "RECIBIDA" -> List.of(INICIAR_DIAGNOSTICO, CANCELAR);
            case "EN_DIAGNOSTICO" -> List.of(ENVIAR_A_APROBACION, CANCELAR);
            case "ESPERANDO_APROBACION" -> List.of(INICIAR_REPARACION, VOLVER_A_DIAGNOSTICO, CANCELAR);
            case "EN_REPARACION" -> List.of(FINALIZAR);
            case "FINALIZADA" -> List.of(ENTREGAR);
            default -> List.of();
        };
    }
}
