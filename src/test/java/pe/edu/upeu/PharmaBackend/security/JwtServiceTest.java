package pe.edu.upeu.PharmaBackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private SecretKey nuevaClave() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    private NimbusJwtDecoder decoder(SecretKey key) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(JwtService.ISSUER));
        return decoder;
    }

    @Test
    void emiteTokenFirmadoConPermisosYExpiracionDeUnaHora() {
        SecretKey key = nuevaClave();
        Instant ahora = Instant.now();
        JwtService service = new JwtService(
                NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build(),
                Clock.fixed(ahora, ZoneOffset.UTC));
        Jwt jwt = decoder(key).decode(service.emitir("admin", List.of("ADMIN"), List.of("CATALOGO_LEER")));
        assertEquals("admin", jwt.getSubject());
        assertEquals(List.of("ADMIN"), jwt.getClaimAsStringList("roles"));
        assertEquals(List.of("CATALOGO_LEER"), jwt.getClaimAsStringList("authorities"));
        assertEquals(Duration.ofHours(1), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
    }

    @Test
    void rechazaOtraFirmaYTokenExpirado() {
        SecretKey key = nuevaClave();
        JwtService vigente = new JwtService(
                NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build(),
                Clock.systemUTC());
        String tokenVigente = vigente.emitir("admin", List.of("ADMIN"), List.of("CATALOGO_LEER"));
        assertThrows(JwtException.class, () -> decoder(nuevaClave()).decode(tokenVigente));

        JwtService expirado = new JwtService(
                NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build(),
                Clock.fixed(Instant.now().minus(Duration.ofHours(2)), ZoneOffset.UTC));
        String tokenExpirado = expirado.emitir("admin", List.of("ADMIN"), List.of("CATALOGO_LEER"));
        assertThrows(JwtException.class, () -> decoder(key).decode(tokenExpirado));
    }
}
