package pe.edu.upeu.PharmaBackend.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;

import java.util.Locale;

/** Traduce fallos de autenticación sin incluir el token ni detalles del decodificador. */
final class MensajeAutenticacion {
    static final String SIN_SESION = "Debe iniciar sesión para acceder a este recurso";
    static final String EXPIRADO = "El token ha expirado. Inicie sesión nuevamente";
    static final String INVALIDO = "El token no es válido";

    private MensajeAutenticacion() { }

    static String de(HttpServletRequest request, AuthenticationException error) {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith("Bearer ")) return SIN_SESION;
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            String detalle = causa.getMessage();
            if (detalle != null) {
                String normalizado = detalle.toLowerCase(Locale.ROOT);
                if (normalizado.contains("expired") || normalizado.contains("expirado")) return EXPIRADO;
            }
        }
        return INVALIDO;
    }
}
