package com.taller.escritorio.controlador;

import com.taller.escritorio.Servicios;
import com.taller.escritorio.Sesion;
import com.taller.escritorio.util.Formato;
import com.taller.escritorio.util.Tarea;
import com.taller.escritorio.vista.DialogoOrden;
import com.taller.escritorio.vista.componentes.Mensajes;
import com.taller.escritorio.vista.editor.EditorAsignacion;
import com.taller.escritorio.vista.editor.EditorConsumo;
import com.taller.escritorio.vista.editor.EditorDiagnostico;
import com.taller.escritorio.vista.editor.EditorPago;
import com.taller.escritorio.vista.editor.EditorPresupuesto;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Presupuesto;
import com.taller.nucleo.entidad.Usuario;
import com.taller.nucleo.excepcion.ReglaNegocioException;

import java.math.BigDecimal;

/** Acciones del detalle de una orden. Cada una llama al servicio y vuelve a leer la orden completa. */
public final class ControladorOrden {

    private final DialogoOrden vista;
    private final int idOrden;
    private DetalleOrden actual;

    public ControladorOrden(DialogoOrden vista, int idOrden) {
        this.vista = vista;
        this.idOrden = idOrden;
    }

    public void cargar() {
        Tarea.ejecutar(vista, this::leer, detalle -> {
            actual = detalle;
            vista.mostrar(detalle);
        });
    }

    private DetalleOrden leer() {
        OrdenServicio orden = Servicios.CONSULTAS.orden(idOrden)
                .orElseThrow(() -> new ReglaNegocioException("La orden #" + idOrden + " ya no existe."));
        var presupuestos = Servicios.CONSULTAS.presupuestos(idOrden);
        BigDecimal aprobado = presupuestos.stream()
                .filter(p -> p.getEstado() == com.taller.nucleo.entidad.EstadoPresupuesto.APROBADO)
                .map(Presupuesto::total).findFirst().orElse(BigDecimal.ZERO);
        return new DetalleOrden(orden, aprobado, Servicios.PAGOS.saldo(idOrden),
                Servicios.CONSULTAS.diagnosticos(idOrden), presupuestos, Servicios.CONSULTAS.asignaciones(idOrden),
                Servicios.CONSULTAS.observaciones(idOrden), Servicios.CONSULTAS.consumos(idOrden),
                Servicios.CONSULTAS.pagos(idOrden), Servicios.ORDENES.historial(idOrden));
    }

    public void cambiarEstado(Transicion transicion) {
        if (transicion.confirmacion() != null && !Mensajes.confirmar(vista, transicion.confirmacion())) {
            return;
        }
        Usuario autoriza = null;
        if (transicion == Transicion.ENTREGAR && actual.saldo().signum() > 0 && !actual.orden().tieneCreditoAutorizado()) {
            if (!Mensajes.confirmar(vista, "La orden tiene un saldo pendiente de " + Formato.dinero(actual.saldo())
                    + ".\nPara entregar el vehículo un administrador debe autorizar el crédito. ¿Solicitar la autorización?")) {
                return;
            }
            autoriza = ControladorAutorizacion.administrador(vista, "Entregar el vehículo con saldo pendiente.");
            if (autoriza == null) {
                return;
            }
        }
        Usuario administrador = autoriza;
        Tarea.ejecutar(vista, () -> aplicar(transicion, administrador), this::cargar);
    }

    private void aplicar(Transicion transicion, Usuario administrador) {
        Usuario usuario = Sesion.usuario();
        switch (transicion) {
            case INICIAR_DIAGNOSTICO -> Servicios.ORDENES.iniciarDiagnostico(idOrden, usuario);
            case ENVIAR_A_APROBACION -> Servicios.ORDENES.enviarAAprobacion(idOrden, usuario);
            case INICIAR_REPARACION -> Servicios.ORDENES.iniciarReparacion(idOrden, usuario);
            case VOLVER_A_DIAGNOSTICO -> Servicios.ORDENES.volverADiagnostico(idOrden, usuario);
            case FINALIZAR -> Servicios.ORDENES.finalizar(idOrden, usuario);
            case CANCELAR -> Servicios.ORDENES.cancelar(idOrden, usuario);
            case ENTREGAR -> {
                if (administrador != null) {
                    Servicios.ORDENES.autorizarCredito(idOrden, administrador);
                }
                Servicios.ORDENES.entregar(idOrden, usuario);
            }
        }
    }

    public void registrarDiagnostico() {
        Formularios.pedir(vista, "Registrar diagnóstico", "Guardar diagnóstico", new EditorDiagnostico(),
                datos -> Servicios.DIAGNOSTICOS.registrar(idOrden, datos.tecnico().getId(), datos.descripcion(), Sesion.usuario()),
                this::cargar);
    }

    public void nuevoPresupuesto() {
        Formularios.pedir(vista, "Nuevo presupuesto", "Guardar presupuesto", new EditorPresupuesto(actual.orden()),
                presupuesto -> Servicios.PRESUPUESTOS.crear(presupuesto, Sesion.usuario()), this::cargar);
    }

    public void aprobar(Presupuesto presupuesto) {
        if (presupuesto != null && Mensajes.confirmar(vista, "¿Aprobar el presupuesto #" + presupuesto.getId()
                + " por " + Formato.dinero(presupuesto.total()) + "?")) {
            Tarea.ejecutar(vista, () -> Servicios.PRESUPUESTOS.aprobar(presupuesto.getId(), Sesion.usuario()), p -> cargar());
        }
    }

    public void rechazar(Presupuesto presupuesto) {
        if (presupuesto != null && Mensajes.confirmar(vista, "¿Rechazar el presupuesto #" + presupuesto.getId() + "?")) {
            Tarea.ejecutar(vista, () -> Servicios.PRESUPUESTOS.rechazar(presupuesto.getId(), Sesion.usuario()), p -> cargar());
        }
    }

    public void asignarTecnico() {
        Formularios.pedir(vista, "Asignar técnico", "Asignar", new EditorAsignacion(),
                tecnico -> Servicios.TAREAS.asignar(idOrden, tecnico.getId(), Sesion.usuario()), this::cargar);
    }

    public void registrarConsumo() {
        Formularios.pedir(vista, "Registrar consumo de repuesto", "Registrar consumo", new EditorConsumo(),
                datos -> Servicios.INVENTARIO.registrarConsumo(idOrden, datos.repuesto().getId(), datos.cantidad(), Sesion.usuario()),
                this::cargar);
    }

    public void registrarPago() {
        Formularios.pedir(vista, "Registrar pago", "Registrar pago", new EditorPago(actual.saldo()),
                datos -> Servicios.PAGOS.registrar(idOrden, datos.monto(), datos.metodo(), Sesion.usuario()), this::cargar);
    }
}
