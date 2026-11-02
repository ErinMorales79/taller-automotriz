package com.taller.escritorio.controlador;

import com.taller.escritorio.Servicios;
import com.taller.escritorio.Sesion;
import com.taller.escritorio.util.Tarea;
import com.taller.escritorio.vista.DialogoOrden;
import com.taller.escritorio.vista.PanelOrdenes;
import com.taller.escritorio.vista.componentes.Mensajes;
import com.taller.escritorio.vista.editor.EditorOrden;
import com.taller.escritorio.vista.editor.EditorOrden.DatosOrden;
import com.taller.nucleo.entidad.OrdenServicio;
import com.taller.nucleo.entidad.Usuario;

/** Lista, filtra y crea órdenes. Regla 1: si ya hay una activa del mismo tipo pide a un administrador. */
public final class ControladorOrdenes {

    private final PanelOrdenes vista;

    public ControladorOrdenes(PanelOrdenes vista) {
        this.vista = vista;
    }

    public void cargar() {
        String estado = vista.estadoElegido();
        String texto = vista.textoBuscado();
        Tarea.ejecutar(vista, () -> Servicios.CONSULTAS.ordenes(estado, texto), vista::mostrar);
    }

    public void abrir(OrdenServicio orden) {
        if (orden != null) {
            new DialogoOrden(vista.ventana(), orden.getId()).setVisible(true);
            cargar();
        }
    }

    public void nueva() {
        Formularios.pedir(vista.ventana(), "Nueva orden de servicio", "Crear orden", new EditorOrden(),
                this::crear, this::cargar);
    }

    private void crear(DatosOrden datos) {
        Usuario autoriza = null;
        if (Servicios.CONSULTAS.existeOrdenActiva(datos.vehiculo().getId(), datos.tipoAtencion().getId())) {
            // Se pregunta por la autorización en el hilo de la interfaz; el servicio vuelve a exigir la regla.
            autoriza = pedirAutorizacion(datos);
            if (autoriza == null) {
                throw new com.taller.nucleo.excepcion.ReglaNegocioException(
                        "No se creó la orden: sin autorización no puede haber dos órdenes activas del mismo tipo para el vehículo.");
            }
        }
        Servicios.ORDENES.crear(datos.vehiculo(), datos.tipoAtencion(), datos.prioridad(), Sesion.usuario(), autoriza);
    }

    private Usuario pedirAutorizacion(DatosOrden datos) {
        Usuario[] resultado = new Usuario[1];
        Runnable pedir = () -> resultado[0] = ControladorAutorizacion.administrador(vista,
                "El vehículo " + datos.vehiculo().getPlaca() + " ya tiene una orden activa de tipo "
                        + datos.tipoAtencion().getNombre() + ".");
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            pedir.run();
        } else {
            try {
                javax.swing.SwingUtilities.invokeAndWait(pedir);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw new IllegalStateException(e.getCause());
            }
        }
        return resultado[0];
    }
}
