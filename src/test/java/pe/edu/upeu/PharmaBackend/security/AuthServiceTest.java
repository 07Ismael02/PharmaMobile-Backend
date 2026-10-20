package pe.edu.upeu.PharmaBackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upeu.PharmaBackend.dto.security.LoginRequestDTO;
import pe.edu.upeu.PharmaBackend.entity.Empleado;
import pe.edu.upeu.PharmaBackend.entity.Rol;
import pe.edu.upeu.PharmaBackend.entity.Usuario;
import pe.edu.upeu.PharmaBackend.repository.UsuarioRepository;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private Usuario usuario() {
        Empleado empleado = new Empleado();
        empleado.setNombres("Nombre");
        empleado.setApellidos("Prueba");
        empleado.setEstado(true);
        Rol rol = new Rol();
        rol.setNombre("ALMACENERO");
        Usuario usuario = new Usuario();
        usuario.setUsername("almacen");
        usuario.setPassword("hash-de-prueba");
        usuario.setEstado(true);
        usuario.setEmpleado(empleado);
        usuario.setRoles(Set.of(rol));
        return usuario;
    }

    @Test
    void loginValidoDevuelveTokenSinHash() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        JwtService jwt = mock(JwtService.class);
        when(repo.findByUsernameIgnoreCase("almacen")).thenReturn(Optional.of(usuario()));
        when(encoder.matches("clave-de-prueba", "hash-de-prueba")).thenReturn(true);
        when(jwt.emitir(anyString(), anyList(), anyList())).thenReturn("token-de-prueba");
        when(jwt.expiracionSegundos()).thenReturn(3600L);
        var respuesta = new AuthService(repo, encoder, jwt)
                .iniciarSesion(new LoginRequestDTO("almacen", "clave-de-prueba"));
        assertEquals("Bearer", respuesta.tipo());
        assertEquals(3600, respuesta.expiraEnSegundos());
        assertEquals("Nombre Prueba", respuesta.nombreCompleto());
        assertEquals(Set.of("CATALOGO_LEER", "CATALOGO_ESCRIBIR"), Set.copyOf(respuesta.permisos()));
        assertFalse(respuesta.toString().contains("token-de-prueba"));
        assertFalse(respuesta.toString().contains("hash-de-prueba"));
    }

    @Test
    void usuarioInexistenteYClaveIncorrectaDanMismaExcepcion() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        AuthService service = new AuthService(repo, encoder, mock(JwtService.class));
        when(repo.findByUsernameIgnoreCase("inexistente")).thenReturn(Optional.empty());
        when(repo.findByUsernameIgnoreCase("almacen")).thenReturn(Optional.of(usuario()));
        assertThrows(CredencialesInvalidasException.class,
                () -> service.iniciarSesion(new LoginRequestDTO("inexistente", "cualquiera")));
        assertThrows(CredencialesInvalidasException.class,
                () -> service.iniciarSesion(new LoginRequestDTO("almacen", "incorrecta")));
    }

    @Test
    void usuarioDesactivadoNoRecibeToken() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario usuario = usuario();
        usuario.setEstado(false);
        when(repo.findByUsernameIgnoreCase("almacen")).thenReturn(Optional.of(usuario));
        JwtService jwt = mock(JwtService.class);
        assertThrows(CredencialesInvalidasException.class,
                () -> new AuthService(repo, mock(PasswordEncoder.class), jwt)
                        .iniciarSesion(new LoginRequestDTO("almacen", "cualquiera")));
        verifyNoInteractions(jwt);
    }
}
