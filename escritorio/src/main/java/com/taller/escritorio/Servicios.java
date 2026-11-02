package com.taller.escritorio;

import com.taller.nucleo.servicio.AuditoriaServicio;
import com.taller.nucleo.servicio.AutenticacionServicio;
import com.taller.nucleo.servicio.ConsultaServicio;
import com.taller.nucleo.servicio.DiagnosticoServicio;
import com.taller.nucleo.servicio.InventarioServicio;
import com.taller.nucleo.servicio.OrdenServicioServicio;
import com.taller.nucleo.servicio.PagoServicio;
import com.taller.nucleo.servicio.PresupuestoServicio;
import com.taller.nucleo.servicio.ReporteServicio;
import com.taller.nucleo.servicio.TareaServicio;
import com.taller.nucleo.servicio.UsuarioServicio;

/** Los servicios de nucleo que usan los controladores (no guardan estado, se comparten). */
public final class Servicios {

    public static final AutenticacionServicio AUTENTICACION = new AutenticacionServicio();
    public static final OrdenServicioServicio ORDENES = new OrdenServicioServicio();
    public static final PresupuestoServicio PRESUPUESTOS = new PresupuestoServicio();
    public static final DiagnosticoServicio DIAGNOSTICOS = new DiagnosticoServicio();
    public static final TareaServicio TAREAS = new TareaServicio();
    public static final InventarioServicio INVENTARIO = new InventarioServicio();
    public static final PagoServicio PAGOS = new PagoServicio();
    public static final ReporteServicio REPORTES = new ReporteServicio();
    public static final UsuarioServicio USUARIOS = new UsuarioServicio();
    public static final AuditoriaServicio AUDITORIA = new AuditoriaServicio();
    public static final ConsultaServicio CONSULTAS = new ConsultaServicio();

    private Servicios() {
    }
}
