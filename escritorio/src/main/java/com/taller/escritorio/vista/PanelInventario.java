package com.taller.escritorio.vista;

import com.taller.escritorio.vista.componentes.PanelPestanas;

/** Inventario: existencias de repuestos, compras a proveedores y proveedores. */
public final class PanelInventario extends PanelPestanas {

    public PanelInventario() {
        agregar("Repuestos", "inventario", Pantallas.repuestos());
        agregar("Compras", "cartera", new PanelCompras());
        agregar("Proveedores", "usuarios", Pantallas.proveedores());
    }
}
