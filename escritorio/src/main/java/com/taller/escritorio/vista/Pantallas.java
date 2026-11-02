package com.taller.escritorio.vista;

import com.taller.escritorio.vista.componentes.ConfiguracionCrud;
import com.taller.escritorio.vista.componentes.TablaDatos.Columna;
import com.taller.escritorio.vista.editor.EditorCliente;
import com.taller.escritorio.vista.editor.EditorMarca;
import com.taller.escritorio.vista.editor.EditorModelo;
import com.taller.escritorio.vista.editor.EditorProveedor;
import com.taller.escritorio.vista.editor.EditorRepuesto;
import com.taller.escritorio.vista.editor.EditorServicio;
import com.taller.escritorio.vista.editor.EditorTecnico;
import com.taller.escritorio.vista.editor.EditorTipoAtencion;
import com.taller.escritorio.vista.editor.EditorVehiculo;
import com.taller.nucleo.entidad.Cliente;
import com.taller.nucleo.entidad.Marca;
import com.taller.nucleo.entidad.Modelo;
import com.taller.nucleo.entidad.Proveedor;
import com.taller.nucleo.entidad.Repuesto;
import com.taller.nucleo.entidad.Servicio;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.TipoAtencion;
import com.taller.nucleo.entidad.Vehiculo;
import com.taller.nucleo.seguridad.Accion;
import com.taller.nucleo.servicio.Catalogos;

import java.util.List;
import java.util.function.Function;

/** Fábrica de las pantallas de catálogo: cada una es un PanelCrud con sus columnas y su formulario. */
public final class Pantallas {

    private Pantallas() {
    }

    private static <T> Columna<T> col(String titulo, Function<T, Object> valor) {
        return new Columna<>(titulo, valor);
    }

    public static PanelCrud<Cliente> clientes() {
        return new PanelCrud<>(new ConfiguracionCrud<>("Clientes", "Propietarios de los vehículos", "cliente", "usuarios",
                List.of(col("Documento", Cliente::getDocumento), col("Nombre", Cliente::getNombre),
                        col("Teléfono", Cliente::getTelefono), col("Correo", Cliente::getCorreo),
                        col("Dirección", Cliente::getDireccion)),
                Catalogos.clientes(), EditorCliente::new, Accion.GESTIONAR_CLIENTES_Y_VEHICULOS,
                Cliente::getNombre, Cliente::getId));
    }

    public static PanelCrud<Vehiculo> vehiculos() {
        return new PanelCrud<>(new ConfiguracionCrud<>("Vehículos", "Vehículos registrados y sus propietarios", "vehículo", "auto",
                List.of(col("Placa", Vehiculo::getPlaca), col("Propietario", v -> v.getPropietario().getNombre()),
                        col("Marca y modelo", v -> v.getModelo().toString()), col("Año", Vehiculo::getAnio),
                        col("Color", Vehiculo::getColor), col("Kilometraje", Vehiculo::getKilometraje)),
                Catalogos.vehiculos(), EditorVehiculo::new, Accion.GESTIONAR_CLIENTES_Y_VEHICULOS,
                Vehiculo::getPlaca, Vehiculo::getId));
    }

    public static PanelCrud<Marca> marcas() {
        return new PanelCrud<>(new ConfiguracionCrud<>("Marcas", "Marcas de vehículos", "marca", "auto",
                List.of(col("Nombre", Marca::getNombre)),
                Catalogos.marcas(), EditorMarca::new, Accion.GESTIONAR_CLIENTES_Y_VEHICULOS,
                Marca::getNombre, Marca::getId));
    }

    public static PanelCrud<Modelo> modelos() {
        return new PanelCrud<>(new ConfiguracionCrud<>("Modelos", "Modelos de cada marca", "modelo", "auto",
                List.of(col("Marca", m -> m.getMarca().getNombre()), col("Modelo", Modelo::getNombre)),
                Catalogos.modelos(), EditorModelo::new, Accion.GESTIONAR_CLIENTES_Y_VEHICULOS,
                Modelo::toString, Modelo::getId));
    }

    public static PanelCrud<Tecnico> tecnicos() {
        return new PanelCrud<>(new ConfiguracionCrud<>("Técnicos", "Personal que atiende las órdenes", "técnico", "usuarios",
                List.of(col("Nombre", Tecnico::getNombre), col("Especialidad", Tecnico::getEspecialidad),
                        col("Activo", Tecnico::isActivo)),
                Catalogos.tecnicos(), EditorTecnico::new, Accion.GESTIONAR_CATALOGOS,
                Tecnico::getNombre, Tecnico::getId));
    }

    public static PanelCrud<Servicio> servicios() {
        return new PanelCrud<>(new ConfiguracionCrud<>("Servicios", "Mano de obra que se cotiza en los presupuestos", "servicio", "ajustes",
                List.of(col("Servicio", Servicio::getNombre), col("Precio base por hora", Servicio::getPrecioBase)),
                Catalogos.servicios(), EditorServicio::new, Accion.GESTIONAR_CATALOGOS,
                Servicio::getNombre, Servicio::getId));
    }

    public static PanelCrud<TipoAtencion> tiposAtencion() {
        return new PanelCrud<>(new ConfiguracionCrud<>("Tipos de atención", "Clasifican las órdenes de servicio", "tipo de atención", "catalogo",
                List.of(col("Nombre", TipoAtencion::getNombre)),
                Catalogos.tiposAtencion(), EditorTipoAtencion::new, Accion.GESTIONAR_CATALOGOS,
                TipoAtencion::getNombre, TipoAtencion::getId));
    }

    public static PanelCrud<Repuesto> repuestos() {
        return new PanelCrud<>(new ConfiguracionCrud<>("Repuestos", "Existencias y precios", "repuesto", "inventario",
                List.of(col("Código", Repuesto::getCodigo), col("Nombre", Repuesto::getNombre),
                        col("Stock", Repuesto::getStock), col("Stock mínimo", Repuesto::getStockMinimo),
                        col("Costo", Repuesto::getPrecioCosto), col("Venta", Repuesto::getPrecioVenta),
                        col("Alerta", r -> r.stockBajo() ? "Stock bajo" : "")),
                Catalogos.repuestos(), EditorRepuesto::new, Accion.GESTIONAR_INVENTARIO,
                Repuesto::getNombre, Repuesto::getId));
    }

    public static PanelCrud<Proveedor> proveedores() {
        return new PanelCrud<>(new ConfiguracionCrud<>("Proveedores", "Quienes surten los repuestos", "proveedor", "usuarios",
                List.of(col("Nombre", Proveedor::getNombre), col("Teléfono", Proveedor::getTelefono)),
                Catalogos.proveedores(), EditorProveedor::new, Accion.GESTIONAR_INVENTARIO,
                Proveedor::getNombre, Proveedor::getId));
    }
}
