package pe.edu.upeu.PharmaBackend.security;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class JwtService {
    public static final long EXPIRACION_SEGUNDOS = 3600;
    public static final String ISSUER = "PharmaSoft";

    private final JwtEncoder encoder;
    private final Clock clock;

    public JwtService(JwtEncoder encoder, Clock clock) {
        this.encoder = encoder;
        this.clock = clock;
    }

    public String emitir(String username, List<String> roles, List<String> permisos) {
        Instant ahora = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(username)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(Duration.ofSeconds(EXPIRACION_SEGUNDOS)))
                .claim("roles", roles)
                .claim("authorities", permisos)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
