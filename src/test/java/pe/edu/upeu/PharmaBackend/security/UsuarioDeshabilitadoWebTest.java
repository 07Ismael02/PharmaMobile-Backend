package pe.edu.upeu.PharmaBackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.PharmaBackend.entity.Empleado;
import pe.edu.upeu.PharmaBackend.entity.Usuario;
import pe.edu.upeu.PharmaBackend.repository.UsuarioRepository;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UsuarioDeshabilitadoWebTest.ProductosDePrueba.class)
@Import({SecurityConfig.class, JwtExpiradoWebTest.ClaveDePrueba.class,
        UsuarioDeshabilitadoWebTest.ProductosDePrueba.class})
class UsuarioDeshabilitadoWebTest {
    @Autowired MockMvc mvc;
    @Autowired JwtEncoder encoder;

    @MockitoBean UsuarioRepository usuarios;

    @Test
    void deshabilitadoTrasAutenticarseRecibe401SegunEstadoActual() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setEstado(true);
        Empleado empleado = new Empleado();
        empleado.setEstado(true);
        usuario.setEmpleado(empleado);
        when(usuarios.findByUsernameIgnoreCase("almacen")).thenReturn(Optional.of(usuario));

        String token = new JwtService(encoder, Clock.systemUTC(), 60)
                .emitir("almacen", List.of("ALMACENERO"), List.of("CATALOGO_LEER"));

        mvc.perform(get("/api/v1/productos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        usuario.setEstado(false);
        mvc.perform(get("/api/v1/productos").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("El usuario está deshabilitado"));

        verify(usuarios, times(2)).findByUsernameIgnoreCase("almacen");
    }

    @RestController
    static class ProductosDePrueba {
        @GetMapping("/api/v1/productos")
        String productos() { return "OK"; }
    }
}
