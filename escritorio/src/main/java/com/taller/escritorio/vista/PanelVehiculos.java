package com.taller.escritorio.vista;

import com.taller.escritorio.vista.componentes.PanelPestanas;

/** Vehículos junto con sus marcas y modelos, que recepción mantiene en el mismo lugar. */
public final class PanelVehiculos extends PanelPestanas {

    public PanelVehiculos() {
        agregar("Vehículos", "auto", Pantallas.vehiculos());
        agregar("Marcas", "catalogo", Pantallas.marcas());
        agregar("Modelos", "catalogo", Pantallas.modelos());
    }
}
