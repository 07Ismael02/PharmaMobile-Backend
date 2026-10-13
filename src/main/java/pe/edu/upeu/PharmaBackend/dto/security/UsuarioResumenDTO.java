package pe.edu.upeu.PharmaBackend.dto.security;

import java.util.List;

public record UsuarioResumenDTO(Long id, String username, Boolean estado, Long empleadoId,
                                List<String> roles) { }
