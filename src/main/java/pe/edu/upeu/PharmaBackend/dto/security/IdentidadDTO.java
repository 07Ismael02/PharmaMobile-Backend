package pe.edu.upeu.PharmaBackend.dto.security;

import java.util.List;

public record IdentidadDTO(String username, String nombreCompleto,
                           List<String> roles, List<String> permisos) { }
