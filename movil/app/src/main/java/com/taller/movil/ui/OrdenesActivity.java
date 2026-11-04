package com.taller.movil.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.taller.movil.AplicacionTaller;
import com.taller.movil.R;
import com.taller.movil.databinding.ActivityOrdenesBinding;
import com.taller.movil.red.Llamadas;

/** Órdenes asignadas al técnico. Se actualiza al abrir, al volver del detalle y al deslizar hacia abajo. */
public class OrdenesActivity extends AppCompatActivity {

    private ActivityOrdenesBinding vista;
    private AplicacionTaller aplicacion;
    private TareaAdapter adaptador;

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        aplicacion = (AplicacionTaller) getApplication();
        if (!aplicacion.sesion().haySesion()) {
            LoginActivity.reiniciar(this);
            return;
        }
        vista = ActivityOrdenesBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());

        String nombre = aplicacion.sesion().tecnico();
        vista.textoSaludo.setText(getString(R.string.saludo, nombre.isEmpty() ? aplicacion.sesion().usuario() : nombre));
        vista.barra.setOnMenuItemClickListener(opcion -> {
            if (opcion.getItemId() == R.id.accion_cerrar_sesion) {
                aplicacion.sesion().cerrarSesion();
                LoginActivity.reiniciar(this);
                return true;
            }
            return false;
        });

        adaptador = new TareaAdapter(tarea -> {
            Intent intento = new Intent(this, DetalleOrdenActivity.class);
            intento.putExtra(DetalleOrdenActivity.EXTRA_ID_ORDEN, tarea.getIdOrden());
            startActivity(intento);
        });
        vista.lista.setLayoutManager(new LinearLayoutManager(this));
        vista.lista.setAdapter(adaptador);
        vista.actualizar.setOnRefreshListener(this::cargar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (vista != null) {
            cargar();
        }
    }

    private void cargar() {
        vista.actualizar.setRefreshing(true);
        Llamadas.ejecutar(aplicacion.api().ordenesAsignadas(), resultado -> {
            if (Pantalla.cerrada(this)) {
                return;
            }
            vista.actualizar.setRefreshing(false);
            if (Pantalla.huboError(this, resultado)) {
                return;
            }
            adaptador.mostrar(resultado.getDato());
            vista.textoVacio.setVisibility(resultado.getDato().isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
