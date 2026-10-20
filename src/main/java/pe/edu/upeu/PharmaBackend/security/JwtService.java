package pe.edu.upeu.PharmaBackend.security;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class JwtService {
    public static final String ISSUER = "PharmaSoft";

    private final JwtEncoder encoder;
    private final Clock clock;
    private final long expiracionSegundos;

    public JwtService(JwtEncoder encoder, Clock clock, long expiracionMinutos) {
        if (expiracionMinutos < 1 || expiracionMinutos > Long.MAX_VALUE / 60) {
            throw new IllegalArgumentException("La expiración JWT debe ser positiva");
        }
        this.encoder = encoder;
        this.clock = clock;
        this.expiracionSegundos = expiracionMinutos * 60;
    }

    public long expiracionSegundos() { return expiracionSegundos; }

    public String emitir(String username, List<String> roles, List<String> permisos) {
        Instant ahora = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(username)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(Duration.ofSeconds(expiracionSegundos)))
                .claim("roles", roles)
                .claim("authorities", permisos)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
