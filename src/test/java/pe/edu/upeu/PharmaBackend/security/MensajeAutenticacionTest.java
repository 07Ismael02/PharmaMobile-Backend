package pe.edu.upeu.PharmaBackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;

import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MensajeAutenticacionTest {
    @Test
    void distingueAusenteMinúsculaAlteradoYExpirado() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        BadCredentialsException error = new BadCredentialsException("fallo de autenticación");
        assertEquals(MensajeAutenticacion.SIN_SESION, MensajeAutenticacion.de(request, error));

        request.addHeader("Authorization", "bearer valor-no-real");
        assertEquals(MensajeAutenticacion.SIN_SESION, MensajeAutenticacion.de(request, error));

        request.removeHeader("Authorization");
        request.addHeader("Authorization", "Bearer valor-no-real");
        assertEquals(MensajeAutenticacion.INVALIDO, MensajeAutenticacion.de(request, error));
        assertEquals(MensajeAutenticacion.EXPIRADO,
                MensajeAutenticacion.de(request, new BadCredentialsException("Jwt expired at ...")));
    }

    @Test
    void clasificaLaExcepcionRealDeUnJwtExpirado() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        var clave = new SecretKeySpec(bytes, "HmacSHA256");
        var emisor = new JwtService(
                NimbusJwtEncoder.withSecretKey(clave).algorithm(MacAlgorithm.HS256).build(),
                Clock.fixed(Instant.now().minusSeconds(180), ZoneOffset.UTC), 1);
        String token = emisor.emitir("almacen", List.of("ALMACENERO"), List.of("CATALOGO_LEER"));
        var decoder = NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
        var proveedor = new JwtAuthenticationProvider(decoder);
        AuthenticationException error = assertThrows(AuthenticationException.class,
                () -> proveedor.authenticate(new BearerTokenAuthenticationToken(token)));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valor-no-real");

        assertEquals(MensajeAutenticacion.EXPIRADO, MensajeAutenticacion.de(request, error));
    }
}
