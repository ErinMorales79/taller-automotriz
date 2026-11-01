package com.taller.nucleo.soporte;

import com.taller.nucleo.dao.jdbc.ClienteDaoJdbc;
import com.taller.nucleo.dao.jdbc.MarcaDaoJdbc;
import com.taller.nucleo.dao.jdbc.ModeloDaoJdbc;
import com.taller.nucleo.dao.jdbc.OrdenServicioDaoJdbc;
import com.taller.nucleo.dao.jdbc.RepuestoDaoJdbc;
import com.taller.nucleo.dao.jdbc.ServicioDaoJdbc;
import com.taller.nucleo.dao.jdbc.TecnicoDaoJdbc;
import com.taller.nucleo.dao.jdbc.TipoAtencionDaoJdbc;
import com.taller.nucleo.dao.jdbc.UsuarioDaoJdbc;
import com.taller.nucleo.dao.jdbc.VehiculoDaoJdbc;
import com.taller.nucleo.entidad.Cliente;
import com.taller.nucleo.entidad.Marca;
import com.taller.nucleo.entidad.Modelo;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Prioridad;
import com.taller.nucleo.entidad.Repuesto;
import com.taller.nucleo.entidad.Rol;
import com.taller.nucleo.entidad.Servicio;
import com.taller.nucleo.entidad.Tecnico;
import com.taller.nucleo.entidad.TipoAtencion;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.entidad.Vehiculo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.UUID;

/** Crea registros de prueba ya guardados en la base (dentro de la transacción de la prueba). */
public final class Datos {

    private final Connection conexion;

    public Datos(Connection conexion) {
        this.conexion = conexion;
    }

    public static String unico(String prefijo) {
        return prefijo + UUID.randomUUID().toString().substring(0, 8);
    }

    public Usuario usuario(Rol rol) {
        return new UsuarioDaoJdbc(conexion).insertar(new Usuario(unico("u"), "hash-ficticio", rol));
    }

    public Tecnico tecnico() {
        return new TecnicoDaoJdbc(conexion).insertar(new Tecnico(unico("Técnico ")));
    }

    public Cliente cliente() {
        return new ClienteDaoJdbc(conexion).insertar(new Cliente(unico("D"), unico("Cliente ")));
    }

    public Modelo modelo() {
        Marca marca = new MarcaDaoJdbc(conexion).insertar(new Marca(unico("Marca ")));
        return new ModeloDaoJdbc(conexion).insertar(new Modelo(marca, unico("Modelo ")));
    }

    public Vehiculo vehiculo() {
        return new VehiculoDaoJdbc(conexion).insertar(new Vehiculo(unico("P"), cliente(), modelo(), 2020));
    }

    public TipoAtencion tipoAtencion() {
        return new TipoAtencionDaoJdbc(conexion).insertar(new TipoAtencion(unico("Tipo ")));
    }

    public Servicio servicio(String precio) {
        return new ServicioDaoJdbc(conexion).insertar(new Servicio(unico("Servicio "), new BigDecimal(precio)));
    }

    public Repuesto repuesto(int stock, String precioVenta) {
        Repuesto repuesto = new Repuesto(unico("R"), unico("Repuesto "), new BigDecimal("1.00"),
                new BigDecimal(precioVenta));
        repuesto.setStock(stock);
        return new RepuestoDaoJdbc(conexion).insertar(repuesto);
    }

    public OrdenServicio orden(Usuario registra) {
        return new OrdenServicioDaoJdbc(conexion).insertar(
                new OrdenServicio(vehiculo(), tipoAtencion(), Prioridad.MEDIA, registra));
    }
}
