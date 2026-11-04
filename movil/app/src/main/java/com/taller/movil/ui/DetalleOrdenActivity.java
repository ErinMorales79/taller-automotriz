package com.taller.movil.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.taller.movil.AplicacionTaller;
import com.taller.movil.R;
import com.taller.movil.databinding.ActivityDetalleBinding;
import com.taller.movil.databinding.ItemRegistroBinding;
import com.taller.movil.red.Llamadas;
import com.taller.movil.red.Modelos.AvanceSolicitud;
import com.taller.movil.red.Modelos.DetalleOrden;
import com.taller.movil.red.Modelos.Diagnostico;
import com.taller.movil.red.Modelos.Observacion;
import com.taller.movil.red.Modelos.ObservacionSolicitud;
import com.taller.movil.util.Texto;

/** Detalle de una orden: datos del vehículo, avance propio, diagnósticos y observaciones. */
public class DetalleOrdenActivity extends AppCompatActivity {

    static final String EXTRA_ID_ORDEN = "idOrden";

    private ActivityDetalleBinding vista;
    private AplicacionTaller aplicacion;
    private int idOrden;
    private int idAsignacion;

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        aplicacion = (AplicacionTaller) getApplication();
        idOrden = getIntent().getIntExtra(EXTRA_ID_ORDEN, -1);
        vista = ActivityDetalleBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        vista.barra.setNavigationOnClickListener(v -> finish());
        vista.deslizadorAvance.addOnChangeListener((deslizador, valor, delUsuario) ->
                vista.textoAvance.setText(getString(R.string.avance_porcentaje, Math.round(valor))));
        vista.botonGuardarAvance.setOnClickListener(v -> guardarAvance());
        vista.botonAgregarObservacion.setOnClickListener(v -> agregarObservacion());
        cargar();
    }

    private void cargar() {
        enMarcha(true);
        Llamadas.ejecutar(aplicacion.api().detalleDeOrden(idOrden), resultado -> {
            if (Pantalla.cerrada(this)) {
                return;
            }
            enMarcha(false);
            if (!Pantalla.huboError(this, resultado)) {
                mostrar(resultado.getDato());
            }
        });
    }

    private void mostrar(DetalleOrden detalle) {
        idAsignacion = detalle.getMiTarea().getIdAsignacion();
        vista.barra.setTitle(getString(R.string.titulo_orden, detalle.getIdOrden()));
        vista.textoPlaca.setText(detalle.getPlaca());
        vista.textoVehiculo.setText(getString(R.string.vehiculo_detalle, detalle.getVehiculo(), detalle.getAnio(),
                detalle.getColor() == null ? "-" : detalle.getColor()));
        vista.textoDatos.setText(getString(R.string.datos_detalle, detalle.getTipoAtencion(),
                Texto.legible(detalle.getPrioridad()), Texto.legible(detalle.getEstado()),
                Texto.fechaHora(detalle.getFechaIngreso())));
        int avance = detalle.getMiTarea().getPorcentajeAvance();
        vista.deslizadorAvance.setValue(avance);
        vista.textoAvance.setText(getString(R.string.avance_porcentaje, avance));

        vista.contenedorDiagnosticos.removeAllViews();
        for (Diagnostico diagnostico : detalle.getDiagnosticos()) {
            agregarRegistro(vista.contenedorDiagnosticos, diagnostico.getTecnico() + "  -  "
                    + Texto.fechaHora(diagnostico.getFecha()), diagnostico.getDescripcion());
        }
        if (detalle.getDiagnosticos().isEmpty()) {
            agregarRegistro(vista.contenedorDiagnosticos, null, getString(R.string.sin_diagnosticos));
        }
        vista.contenedorObservaciones.removeAllViews();
        for (Observacion observacion : detalle.getObservaciones()) {
            agregarRegistro(vista.contenedorObservaciones, observacion.getTecnico() + "  -  "
                    + Texto.fechaHora(observacion.getFecha()), observacion.getTexto());
        }
        if (detalle.getObservaciones().isEmpty()) {
            agregarRegistro(vista.contenedorObservaciones, null, getString(R.string.sin_observaciones));
        }
    }

    private void agregarRegistro(LinearLayout contenedor, String encabezado, String cuerpo) {
        ItemRegistroBinding registro = ItemRegistroBinding.inflate(LayoutInflater.from(this), contenedor, false);
        registro.textoEncabezado.setVisibility(encabezado == null ? View.GONE : View.VISIBLE);
        registro.textoEncabezado.setText(encabezado);
        registro.textoCuerpo.setText(cuerpo);
        contenedor.addView(registro.getRoot());
    }

    private void guardarAvance() {
        enMarcha(true);
        int porcentaje = Math.round(vista.deslizadorAvance.getValue());
        Llamadas.ejecutar(aplicacion.api().actualizarAvance(idAsignacion, new AvanceSolicitud(porcentaje)), resultado -> {
            if (Pantalla.cerrada(this)) {
                return;
            }
            enMarcha(false);
            if (!Pantalla.huboError(this, resultado)) {
                Toast.makeText(this, R.string.avance_guardado, Toast.LENGTH_SHORT).show();
                cargar();
            }
        });
    }

    private void agregarObservacion() {
        CharSequence escrito = vista.entradaObservacion.getText();
        String texto = escrito == null ? "" : escrito.toString().trim();
        if (texto.isEmpty()) {
            Toast.makeText(this, R.string.observacion_vacia, Toast.LENGTH_SHORT).show();
            return;
        }
        enMarcha(true);
        Llamadas.ejecutar(aplicacion.api().agregarObservacion(idOrden, new ObservacionSolicitud(texto)), resultado -> {
            if (Pantalla.cerrada(this)) {
                return;
            }
            enMarcha(false);
            if (!Pantalla.huboError(this, resultado)) {
                vista.entradaObservacion.setText("");
                Toast.makeText(this, R.string.observacion_agregada, Toast.LENGTH_SHORT).show();
                cargar();
            }
        });
    }

    private void enMarcha(boolean trabajando) {
        vista.progreso.setVisibility(trabajando ? View.VISIBLE : View.INVISIBLE);
        vista.botonGuardarAvance.setEnabled(!trabajando);
        vista.botonAgregarObservacion.setEnabled(!trabajando);
    }
}
