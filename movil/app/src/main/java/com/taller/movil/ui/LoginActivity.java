package com.taller.movil.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.taller.movil.AplicacionTaller;
import com.taller.movil.R;
import com.taller.movil.databinding.ActivityLoginBinding;
import com.taller.movil.red.Llamadas;
import com.taller.movil.red.Modelos.LoginRespuesta;
import com.taller.movil.red.Modelos.LoginSolicitud;
import com.taller.movil.util.Servidor;

/** Pantalla de acceso del técnico. Si ya hay una sesión guardada, pasa directo a la lista de órdenes. */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding vista;
    private AplicacionTaller aplicacion;

    /** Vuelve a esta pantalla borrando el resto de la navegación. */
    static void reiniciar(Context contexto) {
        Intent intento = new Intent(contexto, LoginActivity.class);
        intento.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        contexto.startActivity(intento);
    }

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        aplicacion = (AplicacionTaller) getApplication();
        if (aplicacion.sesion().haySesion()) {
            irALasOrdenes();
            return;
        }
        vista = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        vista.entradaServidor.setText(aplicacion.sesion().servidor());
        vista.botonIngresar.setOnClickListener(boton -> ingresar());
        vista.entradaContrasena.setOnEditorActionListener((campo, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_DONE) {
                ingresar();
                return true;
            }
            return false;
        });
    }

    private void ingresar() {
        String usuario = textoDe(vista.entradaUsuario.getText());
        String contrasena = vista.entradaContrasena.getText() == null ? "" : vista.entradaContrasena.getText().toString();
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError(getString(R.string.falta_credenciales));
            return;
        }
        try {
            aplicacion.sesion().guardarServidor(Servidor.normalizar(textoDe(vista.entradaServidor.getText())));
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
            return;
        }
        enMarcha(true);
        Llamadas.ejecutar(aplicacion.api().iniciarSesion(new LoginSolicitud(usuario, contrasena)), resultado -> {
            if (Pantalla.cerrada(this)) {
                return;
            }
            enMarcha(false);
            if (resultado.esExito()) {
                LoginRespuesta respuesta = resultado.getDato();
                aplicacion.sesion().guardarSesion(respuesta.getToken(), respuesta.getUsuario(), respuesta.getTecnico());
                irALasOrdenes();
            } else {
                mostrarError(resultado.getMensaje());
            }
        });
    }

    private static String textoDe(CharSequence texto) {
        return texto == null ? "" : texto.toString().trim();
    }

    private void mostrarError(String mensaje) {
        vista.textoError.setText(mensaje);
        vista.textoError.setVisibility(View.VISIBLE);
    }

    private void enMarcha(boolean trabajando) {
        vista.progreso.setVisibility(trabajando ? View.VISIBLE : View.GONE);
        vista.botonIngresar.setEnabled(!trabajando);
        if (trabajando) {
            vista.textoError.setVisibility(View.GONE);
        }
    }

    private void irALasOrdenes() {
        startActivity(new Intent(this, OrdenesActivity.class));
        finish();
    }
}
