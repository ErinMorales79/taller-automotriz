package com.taller.escritorio.controlador;

import com.taller.escritorio.Servicios;
import com.taller.escritorio.Sesion;
import com.taller.escritorio.util.Tarea;
import com.taller.escritorio.vista.PanelCompras;
import com.taller.escritorio.vista.componentes.DialogoFormulario;
import com.taller.escritorio.vista.componentes.Mensajes;
import com.taller.escritorio.vista.editor.EditorCompra;
import com.taller.nucleo.entidad.Compra;
import com.taller.nucleo.excepcion.ReglaNegocioException;

/** Lista las compras y registra nuevas: el servicio las guarda y suma el stock en una sola transacción. */
public final class ControladorCompras {

    private final PanelCompras vista;

    public ControladorCompras(PanelCompras vista) {
        this.vista = vista;
    }

    public void cargar() {
        Tarea.ejecutar(vista, () -> Servicios.CONSULTAS.compras(Sesion.usuario()), vista::mostrar);
    }

    public void registrar() {
        EditorCompra editor = new EditorCompra();
        new DialogoFormulario(vista.ventana(), "Registrar compra", editor.componente(), "Registrar compra",
                dialogo -> guardar(editor, dialogo)).setVisible(true);
    }

    private void guardar(EditorCompra editor, DialogoFormulario dialogo) {
        Compra compra;
        try {
            compra = editor.construir(null);
        } catch (ReglaNegocioException e) {
            Mensajes.avisar(dialogo, e.getMessage());
            return;
        }
        Tarea.ejecutar(dialogo, () -> Servicios.INVENTARIO.registrarCompra(compra), registrada -> {
            dialogo.cerrar();
            cargar();
        });
    }
}
