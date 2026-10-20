package pe.edu.upeu.PharmaBackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.upeu.PharmaBackend.entity.Usuario;
import pe.edu.upeu.PharmaBackend.repository.UsuarioRepository;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/** Revalida la cuenta en cada solicitud JWT, incluso si el token aún no expiró. */
final class UsuarioVigenteFilter extends OncePerRequestFilter {
    private final UsuarioRepository usuarios;
    private final ObjectMapper mapper;

    UsuarioVigenteFilter(UsuarioRepository usuarios, ObjectMapper mapper) {
        this.usuarios = usuarios;
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion instanceof JwtAuthenticationToken jwt) {
            Usuario usuario = usuarios.findByUsernameIgnoreCase(jwt.getName()).orElse(null);
            if (usuario == null) {
                SecurityConfig.writeError(mapper, request, response, 401, "Unauthorized", MensajeAutenticacion.INVALIDO);
                return;
            }
            if (!Boolean.TRUE.equals(usuario.getEstado()) ||
                    !Boolean.TRUE.equals(usuario.getEmpleado().getEstado())) {
                SecurityConfig.writeError(mapper, request, response, 401, "Unauthorized",
                        "El usuario está deshabilitado");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
