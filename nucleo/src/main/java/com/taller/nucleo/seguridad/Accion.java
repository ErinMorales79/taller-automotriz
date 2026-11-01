package com.taller.nucleo.seguridad;

/** Acciones que se controlan por rol (ver Permisos). */
public enum Accion {
    GESTIONAR_USUARIOS("gestionar usuarios"),
    GESTIONAR_CLIENTES_Y_VEHICULOS("gestionar clientes y vehículos"),
    CREAR_ORDEN("crear órdenes de servicio"),
    GESTIONAR_ORDEN("cambiar el estado de las órdenes"),
    FINALIZAR_ORDEN("finalizar órdenes"),
    GESTIONAR_CATALOGOS("gestionar técnicos, servicios y tipos de atención"),
    GESTIONAR_PRESUPUESTO("crear y resolver presupuestos"),
    ASIGNAR_TECNICOS("asignar técnicos"),
    ACTUALIZAR_AVANCE("actualizar el avance de tareas"),
    GESTIONAR_INVENTARIO("gestionar el inventario y las compras"),
    REGISTRAR_CONSUMO("registrar el consumo de repuestos"),
    REGISTRAR_PAGO("registrar pagos"),
    AUTORIZAR_EXCEPCIONES("autorizar excepciones a las reglas"),
    VER_AUDITORIA("consultar la auditoría"),
    VER_REPORTES("ver reportes");

    private final String descripcion;

    Accion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
