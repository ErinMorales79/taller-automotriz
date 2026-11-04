package com.taller.api.seguridad;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.taller.nucleo.entidad.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/** Emite y verifica los tokens JWT (firmados con HMAC-SHA256) que usa la app móvil. */
@Component
public class ServicioToken {

    private static final int LARGO_MINIMO_SECRETO = 32;

    public record TokenEmitido(String token, Instant expiraEn) {
    }

    private final Algorithm algoritmo;
    private final Duration duracion;

    public ServicioToken(@Value("${taller.jwt.secreto:}") String secreto, @Value("${taller.jwt.horas:8}") long horas) {
        if (secreto == null || secreto.length() < LARGO_MINIMO_SECRETO) {
            throw new IllegalStateException("Falta taller.jwt.secreto en config/api.properties o es muy corto "
                    + "(mínimo " + LARGO_MINIMO_SECRETO + " caracteres). Copia config/api.properties.example y complétalo.");
        }
        if (horas < 1) {
            throw new IllegalStateException("taller.jwt.horas debe ser al menos 1.");
        }
        this.algoritmo = Algorithm.HMAC256(secreto);
        this.duracion = Duration.ofHours(horas);
    }

    public TokenEmitido emitir(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(duracion);
        String token = JWT.create()
                .withSubject(String.valueOf(usuario.getId()))
                .withIssuedAt(ahora)
                .withExpiresAt(expira)
                .sign(algoritmo);
        return new TokenEmitido(token, expira);
    }

    /** El id del usuario dueño del token, o vacío si el token es inválido, falso o está vencido. */
    public Optional<Integer> idUsuario(String token) {
        try {
            return Optional.of(Integer.parseInt(JWT.require(algoritmo).build().verify(token).getSubject()));
        } catch (JWTVerificationException | NumberFormatException e) {
            return Optional.empty();
        }
    }
}
