package pe.edu.upeu.PharmaBackend.security;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import pe.edu.upeu.PharmaBackend.repository.UsuarioRepository;
import pe.edu.upeu.PharmaBackend.exception.dto.ErrorResponseDTO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper mapper,
                                            UsuarioRepository usuarios) throws Exception {
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
                        .bearerTokenResolver(request -> {
                            String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
                            if (cabecera == null || !cabecera.startsWith("Bearer ")) return null;
                            return new DefaultBearerTokenResolver().resolve(request);
                        })
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter))
                        .authenticationEntryPoint((request, response, ex) -> {
                            if (ex instanceof AuthenticationServiceException) {
                                writeError(mapper, request, response, 500, "Internal Server Error",
                                        "No se pudo validar la sesión");
                            } else {
                                writeError(mapper, request, response, 401, "Unauthorized",
                                        MensajeAutenticacion.de(request, ex));
                            }
                        }))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, ex) ->
                                writeError(mapper, request, response, 401, "Unauthorized",
                                        MensajeAutenticacion.de(request, ex)))
                        .accessDeniedHandler((request, response, ex) ->
                                writeError(mapper, request, response, 403, "Forbidden", "No tiene permiso para esta operación")))
                .addFilterAfter(new UsuarioVigenteFilter(usuarios, mapper), BearerTokenAuthenticationFilter.class)
                .build();
    }

    static void writeError(ObjectMapper mapper, HttpServletRequest request,
                                   HttpServletResponse response, int status, String error, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), new ErrorResponseDTO(
                LocalDateTime.now(), status, error, message, request.getRequestURI(), null));
    }
}
