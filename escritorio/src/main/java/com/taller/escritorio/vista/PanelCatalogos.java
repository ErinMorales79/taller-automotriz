package com.taller.escritorio.vista;

import com.taller.escritorio.vista.componentes.PanelPestanas;

/** Catálogos que solo administra el administrador: técnicos, servicios y tipos de atención. */
public final class PanelCatalogos extends PanelPestanas {

    public PanelCatalogos() {
        agregar("Técnicos", "usuarios", Pantallas.tecnicos());
        agregar("Servicios", "ajustes", Pantallas.servicios());
        agregar("Tipos de atención", "catalogo", Pantallas.tiposAtencion());
    }
}
