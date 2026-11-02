package com.taller.escritorio.controlador;

/** Los cinco reportes obligatorios y qué filtros usa cada uno. */
public enum TipoReporte {
    ORDENES_POR_ESTADO("Órdenes por estado y antigüedad", true, false, false),
    PRODUCTIVIDAD("Productividad por técnico", true, false, false),
    REPUESTOS("Repuestos de mayor consumo", true, true, false),
    INGRESOS("Ingresos por servicio", true, false, false),
    HISTORIAL_VEHICULO("Historial de mantenimiento por vehículo", false, false, true);

    private final String titulo;
    private final boolean usaFechas;
    private final boolean usaLimite;
    private final boolean usaVehiculo;

    TipoReporte(String titulo, boolean usaFechas, boolean usaLimite, boolean usaVehiculo) {
        this.titulo = titulo;
        this.usaFechas = usaFechas;
        this.usaLimite = usaLimite;
        this.usaVehiculo = usaVehiculo;
    }

    public boolean usaFechas() {
        return usaFechas;
    }

    public boolean usaLimite() {
        return usaLimite;
    }

    public boolean usaVehiculo() {
        return usaVehiculo;
    }

    @Override
    public String toString() {
        return titulo;
    }
}
