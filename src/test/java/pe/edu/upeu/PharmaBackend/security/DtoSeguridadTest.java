package pe.edu.upeu.PharmaBackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.PharmaBackend.dto.security.LoginRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.security.UsuarioResumenDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoSeguridadTest {
    @Test
    void toStringDeCredencialesNoExponeClave() {
        assertFalse(new LoginRequestDTO("admin", "clave-de-prueba").toString().contains("clave-de-prueba"));
    }

    @Test
    void respuestaDeListadoNoContieneCampoPassword() throws Exception {
        String json = new ObjectMapper().writeValueAsString(
                new UsuarioResumenDTO(1L, "admin", true, 1L, List.of("ADMIN")));
        assertFalse(json.contains("password"));
        assertFalse(json.contains("hash"));
    }
}
