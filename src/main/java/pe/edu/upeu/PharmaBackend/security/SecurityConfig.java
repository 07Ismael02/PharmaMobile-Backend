package pe.edu.upeu.PharmaBackend.security;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import pe.edu.upeu.PharmaBackend.exception.dto.ErrorResponseDTO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper mapper) throws Exception {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> permisos = jwt.getClaimAsStringList("authorities");
            if (permisos == null) return List.of();
            return permisos.stream().map(p -> (GrantedAuthority) new SimpleGrantedAuthority(p)).toList();
        });

        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categorias/**", "/api/v1/productos/**")
                            .hasAuthority("CATALOGO_LEER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/categorias/**", "/api/v1/productos/**")
                            .hasAuthority("CATALOGO_ESCRIBIR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categorias/**", "/api/v1/productos/**")
                            .hasAuthority("CATALOGO_ESCRIBIR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categorias/**", "/api/v1/productos/**")
                            .hasAuthority("CATALOGO_ESCRIBIR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/clientes/**").hasAuthority("CLIENTES_LEER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/clientes/**").hasAuthority("CLIENTES_ESCRIBIR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/clientes/**").hasAuthority("CLIENTES_ESCRIBIR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/clientes/**").hasAuthority("CLIENTES_ESCRIBIR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ventas/**").hasAuthority("VENTAS_LEER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/ventas/**").hasAuthority("VENTAS_REGISTRAR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/reportes/**").hasAuthority("REPORTES_VER")
                        .requestMatchers("/api/v1/usuarios/**", "/api/v1/empleados/**", "/api/v1/roles/**")
                            .hasAuthority("USUARIOS_ADMINISTRAR")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter))
                        .authenticationEntryPoint((request, response, ex) ->
                                writeError(mapper, request, response, 401, "Unauthorized",
                                        ex.getMessage() != null && ex.getMessage().contains("expired")
                                                ? "El token ha expirado"
                                                : "Autenticación requerida o token inválido")))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, ex) ->
                                writeError(mapper, request, response, 401, "Unauthorized", "Autenticación requerida o token inválido"))
                        .accessDeniedHandler((request, response, ex) ->
                                writeError(mapper, request, response, 403, "Forbidden", "No tiene permiso para esta operación")))
                .build();
    }

    private static void writeError(ObjectMapper mapper, HttpServletRequest request,
                                   HttpServletResponse response, int status, String error, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), new ErrorResponseDTO(
                LocalDateTime.now(), status, error, message, request.getRequestURI(), null));
    }
}
