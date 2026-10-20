package pe.edu.upeu.PharmaBackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.PharmaBackend.repository.UsuarioRepository;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = JwtExpiradoWebTest.ProductosDePrueba.class)
@Import({SecurityConfig.class, JwtExpiradoWebTest.ClaveDePrueba.class})
class JwtExpiradoWebTest {
    @Autowired MockMvc mvc;
    @Autowired JwtEncoder encoder;

    @MockitoBean UsuarioRepository usuarios;

    @Test
    void jwtExpiradoDevuelve401YMensajeExacto() throws Exception {
        JwtService emisor = new JwtService(encoder,
                Clock.fixed(Instant.now().minus(Duration.ofHours(2)), ZoneOffset.UTC), 60);
        String token = emisor.emitir("admin", List.of("ADMIN"), List.of("CATALOGO_LEER"));

        mvc.perform(get("/api/v1/productos").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("El token ha expirado. Inicie sesión nuevamente"));
    }

    @RestController
    static class ProductosDePrueba {
        @GetMapping("/api/v1/productos")
        String productos() { return HttpStatus.OK.name(); }
    }

    @TestConfiguration
    @EnableWebSecurity
    static class ClaveDePrueba {
        @Bean
        SecretKey claveDePrueba() {
            byte[] bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
            return new SecretKeySpec(bytes, "HmacSHA256");
        }

        @Bean
        JwtEncoder encoderDePrueba(SecretKey key) {
            return NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build();
        }

        @Bean
        JwtDecoder decoderDePrueba(SecretKey key) {
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                    .macAlgorithm(MacAlgorithm.HS256).build();
            decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(JwtService.ISSUER));
            return decoder;
        }
    }
}
