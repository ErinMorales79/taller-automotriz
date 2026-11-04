package com.taller.movil.datos;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.taller.movil.util.Servidor;

import java.io.IOException;
import java.security.GeneralSecurityException;

/** Guarda el token, el nombre del técnico y la dirección del servidor cifrados en el teléfono. */
public final class AlmacenSesion {

    private static final String ARCHIVO = "sesion_taller";
    private static final String TOKEN = "token";
    private static final String USUARIO = "usuario";
    private static final String TECNICO = "tecnico";
    private static final String SERVIDOR = "servidor";

    private final SharedPreferences preferencias;

    // shortcut: Google marcó security-crypto como obsoleta en la 1.1.0, pero sigue funcionando y es la forma
    // más simple de cifrar el token; cambiar al Keystore de Android directo si se mantiene la app a largo plazo.
    @SuppressWarnings("deprecation")
    public AlmacenSesion(Context contexto) {
        try {
            MasterKey clave = new MasterKey.Builder(contexto).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
            preferencias = EncryptedSharedPreferences.create(contexto, ARCHIVO, clave,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("No se pudo abrir el almacenamiento seguro del teléfono.", e);
        }
    }

    public void guardarSesion(String token, String usuario, String tecnico) {
        preferencias.edit().putString(TOKEN, token).putString(USUARIO, usuario).putString(TECNICO, tecnico).apply();
    }

    public void cerrarSesion() {
        preferencias.edit().remove(TOKEN).remove(USUARIO).remove(TECNICO).apply();
    }

    public String token() {
        return preferencias.getString(TOKEN, null);
    }

    public boolean haySesion() {
        String token = token();
        return token != null && !token.isEmpty();
    }

    public String usuario() {
        return preferencias.getString(USUARIO, "");
    }

    public String tecnico() {
        return preferencias.getString(TECNICO, "");
    }

    public String servidor() {
        return preferencias.getString(SERVIDOR, Servidor.POR_DEFECTO);
    }

    public void guardarServidor(String urlNormalizada) {
        preferencias.edit().putString(SERVIDOR, urlNormalizada).apply();
    }
}
