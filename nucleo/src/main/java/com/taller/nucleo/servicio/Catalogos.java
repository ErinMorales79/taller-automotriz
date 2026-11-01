package com.taller.nucleo.servicio;

import com.taller.nucleo.dao.ClienteDao;
import com.taller.nucleo.dao.MarcaDao;
import com.taller.nucleo.dao.ModeloDao;
import com.taller.nucleo.dao.ProveedorDao;
import com.taller.nucleo.dao.RepuestoDao;
import com.taller.nucleo.dao.ServicioDao;
import com.taller.nucleo.dao.TecnicoDao;
import com.taller.nucleo.dao.TipoAtencionDao;
import com.taller.nucleo.dao.VehiculoDao;
import com.taller.nucleo.dao.jdbc.ClienteDaoJdbc;
import com.taller.nucleo.dao.jdbc.MarcaDaoJdbc;
import com.taller.nucleo.dao.jdbc.ModeloDaoJdbc;
import com.taller.nucleo.dao.jdbc.ProveedorDaoJdbc;
import com.taller.nucleo.dao.jdbc.RepuestoDaoJdbc;
import com.taller.nucleo.dao.jdbc.ServicioDaoJdbc;
import com.taller.nucleo.dao.jdbc.TecnicoDaoJdbc;
import com.taller.nucleo.dao.jdbc.TipoAtencionDaoJdbc;
import com.taller.nucleo.dao.jdbc.VehiculoDaoJdbc;
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

/** Punto de entrada al CRUD de cada catálogo (lo que usarán los formularios de escritorio). */
public final class Catalogos {

    private Catalogos() {
    }

    public static CrudServicio<Cliente, ClienteDao> clientes() {
        return new CrudServicio<>(ClienteDaoJdbc::new, Accion.GESTIONAR_CLIENTES_Y_VEHICULOS, "cliente", Cliente::getId);
    }

    public static CrudServicio<Vehiculo, VehiculoDao> vehiculos() {
        return new CrudServicio<>(VehiculoDaoJdbc::new, Accion.GESTIONAR_CLIENTES_Y_VEHICULOS, "vehiculo", Vehiculo::getId);
    }

    public static CrudServicio<Marca, MarcaDao> marcas() {
        return new CrudServicio<>(MarcaDaoJdbc::new, Accion.GESTIONAR_CLIENTES_Y_VEHICULOS, "marca", Marca::getId);
    }

    public static CrudServicio<Modelo, ModeloDao> modelos() {
        return new CrudServicio<>(ModeloDaoJdbc::new, Accion.GESTIONAR_CLIENTES_Y_VEHICULOS, "modelo", Modelo::getId);
    }

    public static CrudServicio<Tecnico, TecnicoDao> tecnicos() {
        return new CrudServicio<>(TecnicoDaoJdbc::new, Accion.GESTIONAR_CATALOGOS, "tecnico", Tecnico::getId);
    }

    public static CrudServicio<Servicio, ServicioDao> servicios() {
        return new CrudServicio<>(ServicioDaoJdbc::new, Accion.GESTIONAR_CATALOGOS, "servicio", Servicio::getId);
    }

    public static CrudServicio<TipoAtencion, TipoAtencionDao> tiposAtencion() {
        return new CrudServicio<>(TipoAtencionDaoJdbc::new, Accion.GESTIONAR_CATALOGOS, "tipo_atencion", TipoAtencion::getId);
    }

    public static CrudServicio<Repuesto, RepuestoDao> repuestos() {
        return new CrudServicio<>(RepuestoDaoJdbc::new, Accion.GESTIONAR_INVENTARIO, "repuesto", Repuesto::getId);
    }

    public static CrudServicio<Proveedor, ProveedorDao> proveedores() {
        return new CrudServicio<>(ProveedorDaoJdbc::new, Accion.GESTIONAR_INVENTARIO, "proveedor", Proveedor::getId);
    }
}
