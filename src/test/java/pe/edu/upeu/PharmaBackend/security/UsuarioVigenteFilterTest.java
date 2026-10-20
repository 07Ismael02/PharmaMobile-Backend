package pe.edu.upeu.PharmaBackend.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import pe.edu.upeu.PharmaBackend.entity.Empleado;
import pe.edu.upeu.PharmaBackend.entity.Usuario;
import pe.edu.upeu.PharmaBackend.exception.dto.ErrorResponseDTO;
import pe.edu.upeu.PharmaBackend.repository.UsuarioRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioVigenteFilterTest {
    @AfterEach
    void limpiarContexto() { SecurityContextHolder.clearContext(); }

    private void autenticar() {
        Jwt jwt = Jwt.withTokenValue("token-ficticio")
                .header("alg", "HS256").subject("almacen").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @Test
    void cuentaDeshabilitadaCortaLaPeticionCon401() throws Exception {
        autenticar();
        Usuario usuario = new Usuario();
        usuario.setEstado(false);
        Empleado empleado = new Empleado();
        empleado.setEstado(true);
        usuario.setEmpleado(empleado);
        UsuarioRepository repositorio = mock(UsuarioRepository.class);
        when(repositorio.findByUsernameIgnoreCase("almacen")).thenReturn(Optional.of(usuario));
        ObjectMapper mapper = mock(ObjectMapper.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean continuacion = new AtomicBoolean(false);

        new UsuarioVigenteFilter(repositorio, mapper).doFilter(
                new MockHttpServletRequest("GET", "/api/v1/productos"), response,
                (request, respuesta) -> continuacion.set(true));

        assertEquals(401, response.getStatus());
        assertFalse(continuacion.get());
        var error = org.mockito.ArgumentCaptor.forClass(ErrorResponseDTO.class);
        verify(mapper).writeValue(any(java.io.OutputStream.class), error.capture());
        assertEquals("El usuario está deshabilitado", error.getValue().getMessage());
    }

    @Test
    void cuentaActivaContinuaSinBloqueo() throws Exception {
        autenticar();
        Usuario usuario = new Usuario();
        usuario.setEstado(true);
        Empleado empleado = new Empleado();
        empleado.setEstado(true);
        usuario.setEmpleado(empleado);
        UsuarioRepository repositorio = mock(UsuarioRepository.class);
        when(repositorio.findByUsernameIgnoreCase("almacen")).thenReturn(Optional.of(usuario));
        AtomicBoolean continuacion = new AtomicBoolean(false);

        new UsuarioVigenteFilter(repositorio, mock(ObjectMapper.class)).doFilter(
                new MockHttpServletRequest("GET", "/api/v1/productos"), new MockHttpServletResponse(),
                (request, response) -> continuacion.set(true));

        assertTrue(continuacion.get());
    }
}
