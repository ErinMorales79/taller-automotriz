package com.taller.movil.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.taller.movil.R;
import com.taller.movil.databinding.ItemTareaBinding;
import com.taller.movil.red.Modelos.Tarea;
import com.taller.movil.util.Texto;

import java.util.ArrayList;
import java.util.List;

/** Lista de tareas (órdenes asignadas) con su estado y su barra de avance. */
class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.Fila> {

    interface AlElegir {
        void elegida(Tarea tarea);
    }

    private final List<Tarea> tareas = new ArrayList<>();
    private final AlElegir alElegir;

    TareaAdapter(AlElegir alElegir) {
        this.alElegir = alElegir;
    }

    void mostrar(List<Tarea> nuevas) {
        tareas.clear();
        tareas.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Fila(ItemTareaBinding.inflate(LayoutInflater.from(padre.getContext()), padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Tarea tarea = tareas.get(posicion);
        ItemTareaBinding vista = fila.vista;
        vista.textoPlaca.setText(tarea.getPlaca());
        vista.textoEstado.setText(Texto.legible(tarea.getEstadoOrden()));
        vista.textoEstado.setTextColor(colorDe(tarea.getEstadoOrden()));
        vista.textoVehiculo.setText(tarea.getVehiculo());
        vista.textoTipo.setText(vista.getRoot().getContext().getString(R.string.tipo_y_prioridad,
                tarea.getTipoAtencion(), Texto.legible(tarea.getPrioridad())));
        vista.barraAvance.setProgress(tarea.getPorcentajeAvance());
        vista.textoAvance.setText(vista.getRoot().getContext().getString(R.string.avance_porcentaje,
                tarea.getPorcentajeAvance()));
        vista.getRoot().setOnClickListener(v -> alElegir.elegida(tarea));
    }

    @Override
    public int getItemCount() {
        return tareas.size();
    }

    /** Mismos colores de estado que la aplicación de escritorio. */
    static int colorDe(String estado) {
        if (estado == null) {
            return Color.DKGRAY;
        }
        switch (estado) {
            case "RECIBIDA":
                return Color.parseColor("#2563EB");
            case "EN_DIAGNOSTICO":
                return Color.parseColor("#7C3AED");
            case "ESPERANDO_APROBACION":
                return Color.parseColor("#D97706");
            case "EN_REPARACION":
                return Color.parseColor("#0891B2");
            case "FINALIZADA":
                return Color.parseColor("#16A34A");
            case "CANCELADA":
                return Color.parseColor("#DC2626");
            default:
                return Color.parseColor("#475569");
        }
    }

    static final class Fila extends RecyclerView.ViewHolder {
        final ItemTareaBinding vista;

        Fila(ItemTareaBinding vista) {
            super(vista.getRoot());
            this.vista = vista;
        }
    }
}
